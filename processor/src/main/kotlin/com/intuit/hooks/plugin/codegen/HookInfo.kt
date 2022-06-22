package com.intuit.hooks.plugin.codegen

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.ksp.text
import com.intuit.hooks.plugin.ksp.validation.HookAnnotation
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

internal data class HookMember(
    val name: String,
    val visibility: String,
)

internal data class HookSignature(
    val hookFunctionSignatureType: KSTypeReference,
    val hookFunctionSignatureReference: KSCallableReference
) {
    val nullableReturnTypeType = "${returnTypeType}${if (returnTypeType?.last() == '?') "" else "?"}"

    val text get() = hookFunctionSignatureType.text
    val isSuspend get() = hookFunctionSignatureType.modifiers.contains(Modifier.SUSPEND)
    val returnType get() = hookFunctionSignatureReference.returnType.text
    val returnTypePoet get() = hookFunctionSignatureReference.returnType.toTypeName()
    val returnTypeType get() = hookFunctionSignatureReference.returnType.element?.typeArguments?.firstOrNull()?.text
    val returnTypeTypePoet get() = hookFunctionSignatureReference.returnType.element?.typeArguments?.firstOrNull()?.toTypeName()!!
    val nullableReturnTypeTypePoet get() = returnTypeTypePoet.copy(nullable = true)
    val parameters get() = hookFunctionSignatureReference.functionParameters

    override fun toString() = text
}

internal class HookParameter(
    val parameter: KSValueParameter,
    val position: Int,
) {
    val name: String? get() = parameter.name?.asString()
    val type: String get() = parameter.type.text
    val withType get() = "$withoutType: $type"
    val withoutType get() = name ?: "p$position"
}


internal data class HookInfo(
    val property: HookMember,
    val hookType: HookType,
    val hookSignature: HookSignature,
    val params: List<HookParameter>,

    val propertyDeclaration: KSPropertyDeclaration,
    val annotation: HookAnnotation
) {
    // TODO: Should this actually default to public?
    val propertyVisibility get() = propertyDeclaration.getVisibility().toKModifier() ?: KModifier.PUBLIC
    val zeroArity = params.isEmpty()
    val isAsync = hookType.properties.contains(HookProperty.Async)
    val parentResolver get() = (this.propertyDeclaration.parent as? KSClassDeclaration)?.typeParameters?.toTypeParameterResolver() ?: TypeParameterResolver.EMPTY

}

internal val HookInfo.tapMethod get() = if (!zeroArity) """
    public fun tap(name: String, f: $hookSignature): String? = tap(name, generateRandomId(), f)
    public fun tap(name: String, id: String, f: $hookSignature): String? = super.tap(name, id) { _: HookContext, $paramsWithTypes -> f($paramsWithoutTypes) }
""".trimIndent() else ""

internal val HookInfo.paramsWithTypes get() = params.joinToString(transform = HookParameter::withType)
internal val HookInfo.paramsWithoutTypes get() = params.joinToString(transform = HookParameter::withoutType)
internal fun HookInfo.generateClass() = this.hookType.generateClass(this)
internal fun HookInfo.generateProperty() = (if (hookType == HookType.AsyncParallelBailHook) "@kotlinx.coroutines.ExperimentalCoroutinesApi\n" else "") +
    "override val ${property.name}: $className = $className()"
internal fun HookInfo.generateImports(): List<String> = emptyList()

internal val HookInfo.superType get() = this.hookType.toString()

internal val HookInfo.className get() = "${property.name.replaceFirstChar(Char::titlecase)}$superType"
internal val HookInfo.typeParameter get() = "(${if (isAsync) "suspend " else ""}(HookContext, $paramsWithTypes) -> ${hookSignature.returnType})"
internal val HookInfo.interceptParameter get() = "${if (isAsync) "suspend " else ""}(HookContext, $paramsWithTypes) -> Unit"
