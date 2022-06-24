package com.intuit.hooks.plugin.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal data class HooksContainer(
    val name: String,
    val originalClassName: ClassName,
    val typeSpecKind: TypeSpec.Kind,
    val resolvedPackageName: String?,
    val visibilityModifier: KModifier,
    val typeArguments: List<TypeVariableName>,
    val hooks: List<HookInfo>
) {
    val superclass get() = originalClassName.let {
        if (typeArguments.isNotEmpty()) {
            it.parameterizedBy(typeArguments)
        } else
            it
    }
}

internal data class HookSignature(
    val hookFunctionSignatureTypeText: String,
    val isSuspend: Boolean,
    val returnType: TypeName,
    val returnTypeType: TypeName?,
    val hookFunctionSignatureType: TypeName,
) {
    val nullableReturnTypeType: TypeName get() {
        requireNotNull(returnTypeType)
        return returnTypeType.copy(nullable = true)
    }
    override fun toString(): String = hookFunctionSignatureTypeText
}

internal class HookParameter(
    val name: String?,
    val type: TypeName,
    val position: Int,
) {
    val withType get() = "$withoutType: $type"
    val withoutType get() = name ?: "p$position"
}

internal data class HookInfo(
    val property: String,
    val hookType: HookType,
    val hookSignature: HookSignature,
    val params: List<HookParameter>,
    val propertyVisibility: KModifier
) {
    val zeroArity = params.isEmpty()
    val isAsync = hookType.properties.contains(HookProperty.Async)
}

internal val HookInfo.paramsWithTypes get() = params.joinToString(transform = HookParameter::withType)
internal val HookInfo.paramsWithoutTypes get() = params.joinToString(transform = HookParameter::withoutType)
internal val HookInfo.superType get() = this.hookType.toString()
internal val HookInfo.className get() = "${property.replaceFirstChar(Char::titlecase)}$superType"
