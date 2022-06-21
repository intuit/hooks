package com.intuit.hooks.plugin.codegen

import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier
import com.intuit.hooks.plugin.ksp.text

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
    val returnTypeType get() = hookFunctionSignatureReference.returnType.element?.typeArguments?.firstOrNull()?.text

    override fun toString() = text
}

internal class HookParameter(
    val name: String?,
    val type: String,
    val position: Int,
)

internal val HookParameter.withType get() = "$withoutType: $type"
internal val HookParameter.withoutType get() = name ?: "p$position"

internal data class HookInfo(
    val property: HookMember,
    val hookType: HookType,
    val hookSignature: HookSignature,
    val params: List<HookParameter>,
) {
    val zeroArity = params.isEmpty()
    val isAsync = hookType.properties.contains(HookProperty.Async)
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
