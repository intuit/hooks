package com.intuit.hooks.plugin.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal fun HookInfo.generatePoetClass(): TypeSpec = when(this.hookType) {
    // todo: everywhere that we're using ClassName.bestGuess...we should just get the fricking KSP symbols because they have real types
    HookType.SyncHook -> {
        val superclass = ClassName.bestGuess("com.intuit.hooks.${this.superType}")
            .parameterizedBy(
                LambdaTypeName.get(
                    null,
                    listOf(
                        ParameterSpec.unnamed(ClassName.bestGuess("com.intuit.hooks.HookContext"))
                    ) + params.map { ParameterSpec.builder(it.withoutType, ClassName.bestGuess(it.type)).build() },
                    ClassName.bestGuess(hookSignature.returnType),
                )
            )
        TypeSpec.classBuilder(this.className)
            .addModifiers(KModifier.INNER)
            // todo: just get the fricking KSP symbols because they have real types
            .superclass(superclass)
            .build()
    }
    else -> TypeSpec.classBuilder(this.className).build()
}

internal fun HookInfo.generatePoetProperty(): PropertySpec {
    val b = PropertySpec.builder(this.property.name, ClassName.bestGuess(this.className))
        .initializer("${this.className}()")
        .addModifiers(KModifier.OVERRIDE)

    if(this.hookType == HookType.AsyncParallelBailHook) {
        b.addAnnotation(ClassName.bestGuess("kotlinx.coroutines.ExperimentalCoroutinesApi"))
    }

    return b.build()
}