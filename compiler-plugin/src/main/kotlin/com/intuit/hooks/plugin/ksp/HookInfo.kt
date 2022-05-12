package com.intuit.hooks.plugin.ksp

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.intuit.hooks.plugin.codegen.HookCodeGen
import com.intuit.hooks.plugin.codegen.HookParameter
import com.intuit.hooks.plugin.codegen.HookSignature
import com.intuit.hooks.plugin.codegen.HookType


/** Intermediate KSP validation holder for aggregated hook info */
internal data class HookClassInfo(
    val property: KSPropertyDeclaration,
    val hookSignature: HookSignature,
    val hookType: HookType,
    val params: List<HookParameter>
) {
    val zeroArity get() = params.isEmpty()

    fun toCodeGen(): HookCodeGen {
        val propertyName = property.simpleName.asString()
        val visibility = property.getVisibility().name.lowercase()
        return HookCodeGen(hookType, propertyName, params, hookSignature, zeroArity, visibility)
    }
}

/** Wrapper for [KSAnnotation] when we're sure that the annotation is a hook annotation */
@JvmInline internal value class HookAnnotation(val symbol: KSAnnotation) {
    val hookFunctionSignatureType get() = symbol.annotationType.element?.typeArguments?.single()?.type
        ?: throw HooksProcessor.Exception("Could not determine hook function signature type for $symbol")

    val hookFunctionSignatureReference get() = hookFunctionSignatureType.element as? KSCallableReference
        ?: throw HooksProcessor.Exception("Hook type argument must be a function for $symbol")

    val type get() = toString().let(HookType::valueOf)

    override fun toString() = "${symbol.shortName.asString()}Hook"
}
