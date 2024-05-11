package com.intuit.hooks.plugin.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal val hookContext = ClassName.bestGuess("com.intuit.hooks.HookContext")
internal val experimentalCoroutinesAnnotation = ClassName.bestGuess("kotlinx.coroutines.ExperimentalCoroutinesApi")

internal fun HookInfo.createSuperClass(extraTypeName: TypeName? = null): ParameterizedTypeName {
    val lambdaParameter = lambdaTypeName
    val parameters = listOfNotNull(lambdaParameter, extraTypeName)

    return ClassName.bestGuess("com.intuit.hooks.$superType")
        .parameterizedBy(parameters)
}

internal fun generateFile(resolvedPackageName: String, name: String, hookContainers: List<HooksContainer>): FileSpec =
    FileSpec.builder(resolvedPackageName, name).apply {
        hookContainers
            .map(HooksContainer::generateContainerClass)
            .forEach(::addType)
    }.build()

private fun HooksContainer.generateContainerClass(): TypeSpec {
    val className = ClassName.bestGuess(name)
    val builder = when (typeSpecKind) {
        TypeSpec.Kind.INTERFACE,
        TypeSpec.Kind.CLASS -> TypeSpec.classBuilder(className)
        TypeSpec.Kind.OBJECT -> TypeSpec.objectBuilder(className)
    }
    return builder.apply {
        when (typeSpecKind) {
            TypeSpec.Kind.INTERFACE -> addSuperinterface(superclass)
            TypeSpec.Kind.CLASS -> superclass(superclass)
            else -> TODO("Better error")
        }

        addModifiers(visibilityModifier)
        addTypeVariables(typeArguments)

        hooks.forEach {
            addProperty(it.generateProperty())
            addType(it.generateClass())
        }
    }.build()
}

internal val HookInfo.callBuilder get() = FunSpec.builder("call")
    .addParameters(parameterSpecs)
    .apply {
        if (isAsync)
            addModifiers(KModifier.SUSPEND)
    }

internal fun HookInfo.generateClass(): TypeSpec {

    val (superclass, calls) = when (hookType) {
        HookType.SyncHook, HookType.AsyncSeriesHook, HookType.AsyncParallelHook -> {
            val superclass = createSuperClass()

            val call = callBuilder
                .returns(UNIT)
                .addStatement("return super.call { f, context -> f(context, $paramsWithoutTypes) }")

            Pair(superclass, listOf(call))
        }
        HookType.SyncLoopHook, HookType.AsyncSeriesLoopHook -> {
            val superclass = createSuperClass(interceptParameter)

            val call = callBuilder
                .returns(UNIT)
                .addCode(
                    "return super.call(invokeTap = %L, invokeInterceptor = %L)",
                    CodeBlock.of("{ f, context -> f(context, $paramsWithoutTypes) }"),
                    CodeBlock.of("{ f, context -> f(context, $paramsWithoutTypes) }")
                )

            Pair(superclass, listOf(call))
        }
        HookType.SyncWaterfallHook, HookType.AsyncSeriesWaterfallHook -> {
            val superclass = createSuperClass(params.first().type)

            val accumulatorName = params.first().withoutType
            val call = callBuilder
                .returns(hookSignature.returnType)
                .addCode(
                    "return super.call(%N, invokeTap = %L, invokeInterceptor = %L)",
                    accumulatorName,
                    CodeBlock.of("{ f, %N, context -> f(context, $paramsWithoutTypes) }", accumulatorName),
                    CodeBlock.of("{ f, context -> f(context, $paramsWithoutTypes) }")
                )

            Pair(superclass, listOf(call))
        }
        HookType.SyncBailHook, HookType.AsyncSeriesBailHook -> {
            requireNotNull(hookSignature.nullableReturnTypeType)
            val superclass = createSuperClass(hookSignature.returnTypeType)

            val call = callBuilder
                .addParameter(
                    ParameterSpec.builder(
                        "default",
                        LambdaTypeName.get(
                            parameters = parameterSpecs,
                            returnType = hookSignature.returnTypeType!!
                        )
                    ).build()
                )
                .returns(hookSignature.nullableReturnTypeType)
                .addStatement("return call ($paramsWithoutTypes) { _, $paramsWithoutTypes -> default.invoke($paramsWithoutTypes) }")

            val contextCall = callBuilder
                .addParameter(
                    ParameterSpec.builder(
                        "default",
                        createHookContextLambda(hookSignature.returnTypeType).copy(nullable = true)
                    ).defaultValue(CodeBlock.of("null")).build()
                )
                .returns(hookSignature.nullableReturnTypeType)
                .addStatement("return super.call ({ f, context -> f(context, $paramsWithoutTypes) }, default?.let { { context -> default(context, $paramsWithoutTypes) } } )")

            Pair(superclass, listOf(call, contextCall))
        }
        // parallel bail requires the concurrency parameter, otherwise it would be just like the other bail hooks
        HookType.AsyncParallelBailHook -> {
            requireNotNull(hookSignature.nullableReturnTypeType)
            val superclass = createSuperClass(hookSignature.returnTypeType)

            val call = with(callBuilder) {
                // force the concurrency parameter to be first
                parameters.add(0, ParameterSpec("concurrency", INT))

                returns(hookSignature.nullableReturnTypeType)
                    .addStatement("return super.call(concurrency) { f, context -> f(context, $paramsWithoutTypes) }")
            }

            Pair(superclass, listOf(call))
        }
    }

    return TypeSpec.classBuilder(className).apply {
        addModifiers(propertyVisibility, KModifier.INNER)
        addFunctions(tapMethods)
        hookType.addedAnnotation?.let(::addAnnotation)
        superclass(superclass)
        addFunctions(calls.map { it.build() })
    }.build()
}

private val HookInfo.interceptParameter get() = createHookContextLambda(UNIT)
private val HookInfo.lambdaTypeName get() = createHookContextLambda(hookSignature.returnType)

private fun HookInfo.createHookContextLambda(returnType: TypeName): LambdaTypeName {
    val get = LambdaTypeName.get(
        parameters = listOf(ParameterSpec.unnamed(hookContext)) + parameterSpecs,
        returnType = returnType
    )

    return if (this.isAsync) get.copy(suspending = true) else get
}

private val HookInfo.tapMethods: List<FunSpec>
    get() {
        // zero arity functions cause the compiler to be unable to resolve ambiguous references
        if (zeroArity) return emptyList()

        val returnType = STRING.copy(nullable = true)
        val nameParameter = ParameterSpec.builder("name", STRING).build()
        val idParameter = ParameterSpec.builder("id", STRING).build()
        val functionParameter = ParameterSpec.builder("f", hookSignature.hookFunctionSignatureType).build()

        val tap = FunSpec.builder("tap")
            .returns(returnType)
            .addParameter(nameParameter)
            .addParameter(functionParameter)
            .addStatement("return tap(name, generateRandomId(), f)")
            .build()

        val tapWithId = FunSpec.builder("tap")
            .returns(returnType)
            .addParameter(nameParameter)
            .addParameter(idParameter)
            .addParameter(functionParameter)
            .addStatement("return super.tap(name, id) { _: HookContext, $paramsWithTypes -> f($paramsWithoutTypes)}")
            .build()

        return listOf(tap, tapWithId)
    }

internal val HookInfo.parameterSpecs get() = params.map {
    ParameterSpec.builder(it.withoutType, it.type).build()
}

internal val HookType.addedAnnotation: AnnotationSpec? get() = when (this) {
    HookType.AsyncParallelBailHook -> AnnotationSpec.builder(experimentalCoroutinesAnnotation).build()
    else -> null
}

internal fun HookInfo.generateProperty(): PropertySpec =
    PropertySpec.builder(property, ClassName.bestGuess(className)).apply {
        initializer("$className()")
        addModifiers(KModifier.OVERRIDE, propertyVisibility)
        hookType.addedAnnotation?.let(::addAnnotation)
    }.build()
