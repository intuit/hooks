package com.intuit.hooks.plugin

import arrow.core.*
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty

internal sealed class HookValidationError(val msg: String, val property: PsiElement) {
    data class AsyncHookWithoutSuspend(val h: HookClassInfo) : HookValidationError("Async hooks must be defined with a suspend function signature", h.property)
    data class WaterfallMustHaveParameters(val h: HookClassInfo) : HookValidationError("Waterfall hooks must take at least one parameter", h.property)
    data class WaterfallParameterTypeMustMatch(val h: HookClassInfo) : HookValidationError("Waterfall hooks must specify the same types for the first parameter and the return type", h.property)
    data class MustBeInitializedWithDSLMethod(val p: KtProperty) : HookValidationError("${p.name} property needs to be initialized with a DSL method", p)
    data class MustBeHookTypeSignature(val p: KtProperty) : HookValidationError("${p.name} property requires a hook type signature", p)
    data class NoCodeGenerator(val superType: String, val p: KtProperty) : HookValidationError("This hook plugin has no code generator for $superType", p)
}

internal fun validateHook(property: KtProperty): Validated<NonEmptyList<HookValidationError>, HookCodeGen> =
    validateHookType(property).withEither { e ->
        e.flatMap { h -> validateHookProperties(h).toEither() }
    }

private fun validateHookProperties(hook: HookClassInfo) =
    hook.hookType.properties.map { it.validate(hook) }
        .sequenceValidated()
        .map { hook.toCodeGen() }

private fun validateHookType(property: KtProperty): ValidatedNel<HookValidationError, HookClassInfo> {
    val ktCallExpression = property.initializer as? KtCallExpression
    return mustBeInitializedWithDSLMethod(property, ktCallExpression).zip(
        mustBeHookType(property, ktCallExpression),
        hasCodeGenerator(property, ktCallExpression),
        validateParameters(property, ktCallExpression),
    ) { _, signature, hookType, parameters ->
        HookClassInfo(property, signature, hookType, parameters)
    }
}

private fun validateParameters(property: KtProperty, ktCallExpression: KtCallExpression?): ValidatedNel<HookValidationError, List<HookParameter>> {
    val typeReference = ktCallExpression?.typeArguments?.firstOrNull()?.typeReference
    val functionType = typeReference?.typeElement as? KtFunctionType
    val params = functionType?.parameters?.mapIndexed { i: Int, p: KtParameter ->
        val name = p.name
        val type = p.typeReference!!.text
        HookParameter(name, type, i)
    }
    return params?.valid() ?: HookValidationError.MustBeHookTypeSignature(property).invalidNel()
}

private fun hasCodeGenerator(property: KtProperty, ktCallExpression: KtCallExpression?): ValidatedNel<HookValidationError, HookType> {
    val superType = ktCallExpression?.calleeExpression?.text?.capitalizeFirstLetter() ?: ""
    return try {
        HookType.valueOf(superType).valid()
    } catch (e: Exception) {
        HookValidationError.NoCodeGenerator(superType, property).invalidNel()
    }
}

private fun mustBeHookType(property: KtProperty, ktCallExpression: KtCallExpression?): ValidatedNel<HookValidationError, HookSignature> {
    val typeReference = ktCallExpression?.typeArguments?.firstOrNull()?.typeReference
    val hookSignature = typeReference?.let(::HookSignature)
    return hookSignature?.valid() ?: HookValidationError.MustBeHookTypeSignature(property).invalidNel()
}

private fun mustBeInitializedWithDSLMethod(property: KtProperty, ktCallExpression: KtCallExpression?): ValidatedNel<HookValidationError, KtCallExpression> =
    ktCallExpression?.valid() ?: HookValidationError.MustBeInitializedWithDSLMethod(property).invalidNel()
