package com.intuit.hooks.plugin.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName

internal val hookContext = ClassName.bestGuess("com.intuit.hooks.HookContext")
internal fun HookInfo.generatePoetClass(): TypeSpec = when(this.hookType) {
    // todo: everywhere that we're using ClassName.bestGuess...we should just get the fricking KSP symbols because they have real types
    HookType.SyncHook -> {
        val superclass = ClassName.bestGuess("com.intuit.hooks.${this.superType}")
            .parameterizedBy(
                LambdaTypeName.get(
                    null,
                    listOf(
                        ParameterSpec.unnamed(hookContext)
                    ) + paramsWithTypesPoet,
                    hookSignature.returnTypePoet,
                )
            )

        val call = FunSpec.builder("call")
            .addParameters(paramsWithTypesPoet)
            .returns(Unit::class.asTypeName())
            .addStatement("return super.call { f, context -> f(context, ${this.paramsWithoutTypes}) }")
            .build()

        TypeSpec.classBuilder(this.className)
            .addModifiers(KModifier.INNER)
            .superclass(superclass)
            .addFunction(call)
            .addFunctions(tapMethodsPoet)
            .build()
    }
    HookType.SyncBailHook -> {
        val superclass = ClassName.bestGuess("com.intuit.hooks.${this.superType}")
            .parameterizedBy(
                LambdaTypeName.get(
                    null,
                    listOf(
                        ParameterSpec.unnamed(hookContext)
                    ) + paramsWithTypesPoet,
                    hookSignature.returnTypePoet,
                ),
                hookSignature.returnTypeTypePoet!!
            )

        val call = FunSpec.builder("call")
            .addParameters(paramsWithTypesPoet)
            .returns(hookSignature.returnTypeTypePoet?.copy(nullable = true)!!)
            .addStatement("return super.call { f, context -> f(context, ${this.paramsWithoutTypes}) }")
            .build()

        TypeSpec.classBuilder(this.className)
            .addModifiers(KModifier.INNER)
            .superclass(superclass)
            .addFunction(call)
            .addFunctions(tapMethodsPoet)
            .build()

    }
    HookType.AsyncSeriesHook -> {
        val superclass = ClassName.bestGuess("com.intuit.hooks.${this.superType}")
            .parameterizedBy(
                LambdaTypeName.get(
                    null,
                    listOf(
                        ParameterSpec.unnamed(hookContext)
                    ) + paramsWithTypesPoet,
                    hookSignature.returnTypePoet,
                ).copy(suspending = true)
            )

        val call = FunSpec.builder("call")
            .addParameters(paramsWithTypesPoet)
            .addModifiers(KModifier.SUSPEND)
            .returns(Unit::class.asTypeName())
            .addStatement("return super.call { f, context -> f(context, ${this.paramsWithoutTypes}) }")
            .build()

        TypeSpec.classBuilder(this.className)
            .addModifiers(KModifier.INNER)
            .superclass(superclass)
            .addFunction(call)
            .addFunctions(tapMethodsPoet)
            .build()

    }
    HookType.AsyncSeriesBailHook -> {
        val superclass = ClassName.bestGuess("com.intuit.hooks.${this.superType}")
            .parameterizedBy(
                LambdaTypeName.get(
                    null,
                    listOf(
                        ParameterSpec.unnamed(hookContext)
                    ) + paramsWithTypesPoet,
                    hookSignature.returnTypePoet,
                ).copy(suspending = true),
                hookSignature.returnTypeTypePoet!!
            )

        val call = FunSpec.builder("call")
            .addParameters(paramsWithTypesPoet)
            .addModifiers(KModifier.SUSPEND)
            .returns(hookSignature.returnTypeTypePoet?.copy(nullable = true)!!)
            .addStatement("return super.call { f, context -> f(context, ${this.paramsWithoutTypes}) }")
            .build()

        TypeSpec.classBuilder(this.className)
            .addModifiers(KModifier.INNER)
            .superclass(superclass)
            .addFunction(call)
            .addFunctions(tapMethodsPoet)
            .build()

    }
    else -> TypeSpec.classBuilder(this.className).build()
}

private val HookInfo.tapMethodsPoet : List<FunSpec>
    get() {
        val returnType = String::class.asTypeName().copy(nullable = true)
        val nameParameter = ParameterSpec.builder("name", String::class.asTypeName()).build()
        val idParameter = ParameterSpec.builder("id", String::class.asTypeName()).build()
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
            .addStatement("return super.tap(name, id) { _: HookContext, ${this.paramsWithTypes} -> f($paramsWithoutTypes)}")
            .build()

        return listOf(tap, tapWithId)
    }

internal val HookInfo.paramsWithTypesPoet get() = params.map { ParameterSpec.builder(it.withoutType, ClassName.bestGuess(it.type)).build() }

internal fun HookInfo.generatePoetProperty(): PropertySpec {
    val b = PropertySpec.builder(this.property.name, ClassName.bestGuess(this.className))
        .initializer("${this.className}()")
        .addModifiers(KModifier.OVERRIDE)

    if(this.hookType == HookType.AsyncParallelBailHook) {
        b.addAnnotation(ClassName.bestGuess("kotlinx.coroutines.ExperimentalCoroutinesApi"))
    }

    return b.build()
}