package com.intuit.hooks.plugin.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName

internal val hookContext = ClassName.bestGuess("com.intuit.hooks.HookContext")
internal val experimentalCoroutinesAnnotation = ClassName.bestGuess("kotlinx.coroutines.ExperimentalCoroutinesApi")

internal fun HookInfo.generatePoetClass(): TypeSpec {
    // TODO: is there a way to avoid bestGuess here?
    val superclassBuilder = ClassName.bestGuess("com.intuit.hooks.${this.superType}")

    val typeSpecBuilder = TypeSpec.classBuilder(this.className)
        .addModifiers(this.propertyVisibility, KModifier.INNER)
        .addFunctions(tapMethodsPoet)

    return when(this.hookType) {
        HookType.SyncHook -> {
            val superclass = superclassBuilder.parameterizedBy(lambdaTypeName)

            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .returns(UNIT)
                .addStatement("return super.call { f, context -> f(context, ${paramsWithoutTypes}) }")
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.SyncBailHook -> {
            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName,
                    hookSignature.returnTypeTypePoet
                )

            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .returns(hookSignature.returnTypeTypePoet.copy(nullable = true))
                .addStatement("return super.call { f, context -> f(context, ${paramsWithoutTypes}) }")
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.SyncLoopHook -> {
            val interceptParameter = LambdaTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(hookContext)) + paramsWithTypesPoet,
                returnType = UNIT
            ).let { if(isAsync) it.copy(suspending = true) else it }

            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName,
                    interceptParameter
                )

            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .returns(UNIT)
                .addCode("return super.call(invokeTap = %L, invokeInterceptor = %L)",
                    CodeBlock.of("{ f, context -> f(context, ${paramsWithoutTypes}) }"),
                    CodeBlock.of("{ f, context -> f(context, ${paramsWithoutTypes}) }")
                )
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.SyncWaterfallHook -> {
            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName,
                    hookSignature.parameters.first().type.toTypeName()
                )

            val accumulatorName = params.first().withoutType
            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .returns(hookSignature.returnTypePoet)
                .addCode("return super.call(%N, invokeTap = %L, invokeInterceptor = %L)",
                    accumulatorName,
                    CodeBlock.of("{ f, %N, context -> f(context, ${paramsWithoutTypes}) }", accumulatorName),
                    CodeBlock.of("{ f, context -> f(context, ${paramsWithoutTypes}) }")
                )
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.AsyncSeriesHook -> {
            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName.copy(suspending = true)
                )

            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .addModifiers(KModifier.SUSPEND)
                .returns(UNIT)
                .addStatement("return super.call { f, context -> f(context, ${paramsWithoutTypes}) }")
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.AsyncParallelHook -> {
            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName.copy(suspending = true)
                )

            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .addModifiers(KModifier.SUSPEND)
                .returns(UNIT)
                .addStatement("return super.call { f, context -> f(context, ${paramsWithoutTypes}) }")
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.AsyncParallelBailHook -> {
            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName.copy(suspending = true),
                    this.hookSignature.returnTypeTypePoet
                )

            val call = FunSpec.builder("call")
                .addParameter("concurrency", INT)
                .addParameters(paramsWithTypesPoet)
                .addModifiers(KModifier.SUSPEND)
                .returns(hookSignature.returnTypeTypePoet.copy(nullable = true))
                .addStatement("return super.call(concurrency) { f, context -> f(context, ${paramsWithoutTypes}) }")
                .build()

            typeSpecBuilder
                .addAnnotation(experimentalCoroutinesAnnotation)
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.AsyncSeriesBailHook -> {
            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName.copy(suspending = true),
                    hookSignature.returnTypeTypePoet
                )

            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .addModifiers(KModifier.SUSPEND)
                .returns(hookSignature.returnTypeTypePoet.copy(nullable = true))
                .addStatement("return super.call { f, context -> f(context, ${paramsWithoutTypes}) }")
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.AsyncSeriesWaterfallHook -> {
            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName.copy(suspending = true),
                    hookSignature.parameters.first().type.toTypeName()
                )

            val accumulatorName = params.first().withoutType
            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .addModifiers(KModifier.SUSPEND)
                .returns(hookSignature.returnTypePoet)
                .addCode("return super.call(%N, invokeTap = %L, invokeInterceptor = %L)",
                    accumulatorName,
                    CodeBlock.of("{ f, %N, context -> f(context, ${paramsWithoutTypes}) }", accumulatorName),
                    CodeBlock.of("{ f, context -> f(context, ${paramsWithoutTypes}) }")
                )
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
        HookType.AsyncSeriesLoopHook -> {
            val interceptParameter = LambdaTypeName.get(
                parameters = listOf(ParameterSpec.unnamed(hookContext)) + paramsWithTypesPoet,
                returnType = UNIT
            ).let { if(isAsync) it.copy(suspending = true) else it }

            val superclass = superclassBuilder
                .parameterizedBy(
                    lambdaTypeName.copy(suspending = true),
                    interceptParameter
                )

            val call = FunSpec.builder("call")
                .addParameters(paramsWithTypesPoet)
                .addModifiers(KModifier.SUSPEND)
                .returns(UNIT)
                .addCode("return super.call(invokeTap = %L, invokeInterceptor = %L)",
                    CodeBlock.of("{ f, context -> f(context, ${paramsWithoutTypes}) }"),
                    CodeBlock.of("{ f, context -> f(context, ${paramsWithoutTypes}) }")
                )
                .build()

            typeSpecBuilder
                .superclass(superclass)
                .addFunction(call)
                .build()
        }
    }
}

private val HookInfo.lambdaTypeName get() = LambdaTypeName.get(
    null,
    listOf(
        ParameterSpec.unnamed(hookContext)
    ) + paramsWithTypesPoet,
    hookSignature.returnTypePoet,
)

private val HookInfo.tapMethodsPoet : List<FunSpec>
    get() {
        val returnType = STRING.copy(nullable = true)
        val nameParameter = ParameterSpec.builder("name", STRING).build()
        val idParameter = ParameterSpec.builder("id", STRING).build()
        val functionParameter = ParameterSpec.builder("f", this.hookSignature.hookFunctionSignatureType.toTypeName()).build()

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

internal val HookInfo.paramsWithTypesPoet get() = params.map {
    ParameterSpec.builder(it.withoutType, it.parameter.type.toTypeName()).build()
}


internal fun HookInfo.generatePoetProperty(): PropertySpec {
    val b = PropertySpec.builder(this.property.name, ClassName.bestGuess(this.className))
        .initializer("${this.className}()")
            // TODO: the visibility here might not be correct
        .addModifiers(KModifier.OVERRIDE, this.propertyVisibility)

    if(this.hookType == HookType.AsyncParallelBailHook) {
        b.addAnnotation(experimentalCoroutinesAnnotation)
    }

    return b.build()
}