package com.intuit.hooks.plugin.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal val hookContext = ClassName.bestGuess("com.intuit.hooks.HookContext")
internal val experimentalCoroutinesAnnotation = ClassName.bestGuess("kotlinx.coroutines.ExperimentalCoroutinesApi")

internal fun HookInfo.createSuperClass(extraTypeName: TypeName? = null): ParameterizedTypeName {
    val lambdaParameter = lambdaTypeName.suspendIfAsync(this)
    val parameters = listOfNotNull(lambdaParameter, extraTypeName)

    // TODO: is there a way to avoid bestGuess here?
    return ClassName.bestGuess("com.intuit.hooks.$superType")
        .parameterizedBy(parameters)
}

internal fun getTypeSpecBuilder(kind: TypeSpec.Kind, className: ClassName): TypeSpec.Builder =
    when (kind) {
        TypeSpec.Kind.INTERFACE -> TypeSpec.interfaceBuilder(className)
        TypeSpec.Kind.CLASS -> TypeSpec.classBuilder(className)
        TypeSpec.Kind.OBJECT -> TypeSpec.objectBuilder(className)
    }

internal fun HooksContainer.generateFile(): FileSpec {
    val hooksImplClass = generateContainerClass()

    return FileSpec.builder(resolvedPackageName ?: "", name)
        .addType(hooksImplClass)
        .build()
}

private fun HooksContainer.generateContainerClass(): TypeSpec {
    val hooksImplClass = getTypeSpecBuilder(typeSpecKind, ClassName.bestGuess(name)).apply {
        superclass(superclass)
        addModifiers(visibilityModifier)
        addTypeVariables(typeArguments)

        hooks.forEach {
            addProperty(it.generateProperty())
            addType(it.generateClass())
        }
    }.build()
    return hooksImplClass
}

internal fun HookInfo.generateClass(): TypeSpec {
    val callBuilder = FunSpec.builder("call")
        .addParameters(parameterSpecs)
        .suspendIfAsync(this)

    val (superclass, call) = when (hookType) {
        HookType.SyncHook, HookType.AsyncSeriesHook, HookType.AsyncParallelHook  -> {
            val superclass = createSuperClass()

            val call = callBuilder
                .returns(UNIT)
                .addStatement("return super.call { f, context -> f(context, $paramsWithoutTypes) }")

            Pair(superclass, call)
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

            Pair(superclass, call)
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

            Pair(superclass, call)
        }
        HookType.SyncBailHook, HookType.AsyncSeriesBailHook -> {
            requireNotNull(hookSignature.nullableReturnTypeType)
            val superclass = createSuperClass(hookSignature.returnTypeType)

            val call = callBuilder
                .returns(hookSignature.nullableReturnTypeType)
                .addStatement("return super.call { f, context -> f(context, $paramsWithoutTypes) }")

            Pair(superclass, call)
        }
        // parallel bail requires the concurrency parameter, otherwise it would be just like the other bail hooks
        HookType.AsyncParallelBailHook -> {
            requireNotNull(hookSignature.nullableReturnTypeType)
            val superclass = createSuperClass(hookSignature.returnTypeType)

            // force the concurrency parameter to be first
            callBuilder.parameters.add(0, ParameterSpec("concurrency", INT))

            val call = callBuilder
                .returns(hookSignature.nullableReturnTypeType)
                .addStatement("return super.call(concurrency) { f, context -> f(context, $paramsWithoutTypes) }")

            Pair(superclass, call)
        }
    }

    return TypeSpec.classBuilder(className).apply {
        addModifiers(propertyVisibility, KModifier.INNER)
        addFunctions(tapMethods)
        if (hookType == HookType.AsyncParallelBailHook) {
            addAnnotation(experimentalCoroutinesAnnotation)
        }
        superclass(superclass)
        addFunction(call.build())
    }.build()
}

private val HookInfo.interceptParameter get() = LambdaTypeName.get(
    parameters = listOf(ParameterSpec.unnamed(hookContext)) + parameterSpecs,
    returnType = UNIT
).suspendIfAsync(this)

private fun FunSpec.Builder.suspendIfAsync(hookInfo: HookInfo) : FunSpec.Builder = apply {
    if (hookInfo.isAsync) {
        addModifiers(KModifier.SUSPEND)
    }
}

private fun LambdaTypeName.suspendIfAsync(hookInfo: HookInfo): LambdaTypeName =
    if (hookInfo.isAsync) this.copy(suspending = true) else this

private val HookInfo.lambdaTypeName get() = LambdaTypeName.get(
    null,
    listOf(
        ParameterSpec.unnamed(hookContext)
    ) + parameterSpecs,
    hookSignature.returnType
)

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

internal fun HookInfo.generateProperty(): PropertySpec =
    PropertySpec.builder(property, ClassName.bestGuess(className)).apply {
        initializer("$className()")
        // TODO: the visibility here might not be correct
        addModifiers(KModifier.OVERRIDE, propertyVisibility)

        if (hookType == HookType.AsyncParallelBailHook) {
            addAnnotation(experimentalCoroutinesAnnotation)
        }
    }.build()
