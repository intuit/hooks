package com.intuit.hooks.plugin

import org.jetbrains.kotlin.psi.*

internal data class HookSignature(val typeReference: KtTypeReference) {
    val functionType get() = typeReference.typeElement as KtFunctionType
    // todo: we should potentially validate anything with a !! in it
    val returnType get() = functionType.returnTypeReference?.text!!
    val returnTypeType = functionType.returnTypeReference?.typeElement?.typeArgumentsAsTypes?.firstOrNull()?.text

    override fun toString(): String = typeReference.text
}

internal data class HookClassInfo(
    val property: KtProperty,
    val hookSignature: HookSignature,
    val hookType: HookType,
    val params: List<HookParameter>
) {
    val zeroArity get() = params.isEmpty()

    fun toCodeGen(): HookCodeGen {
        // todo: we should potentially validate anything with a !! in it
        val propertyName = property.name!!
        return HookCodeGen(hookType, propertyName, params, hookSignature, zeroArity)
    }
}
