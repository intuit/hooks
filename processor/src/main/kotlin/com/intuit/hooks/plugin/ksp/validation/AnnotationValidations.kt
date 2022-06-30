package com.intuit.hooks.plugin.ksp.validation

import arrow.core.*
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.codegen.HookParameter
import com.intuit.hooks.plugin.codegen.HookSignature
import com.intuit.hooks.plugin.codegen.HookType
import com.intuit.hooks.plugin.codegen.HookType.Companion.annotationDslMarkers
import com.intuit.hooks.plugin.ksp.HooksProcessor
import com.intuit.hooks.plugin.ksp.text
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

/** Wrapper for [KSAnnotation] when we're sure that the annotation is a hook annotation */
@JvmInline internal value class HookAnnotation(val symbol: KSAnnotation) {
    val hookFunctionSignatureType get() = symbol.annotationType.element?.typeArguments?.single()?.type
        ?: throw HooksProcessor.Exception("Could not determine hook function signature type for $symbol")

    val hookFunctionSignatureReference get() = hookFunctionSignatureType.element as? KSCallableReference
        ?: throw HooksProcessor.Exception("Hook type argument must be a function for $symbol")

    val type get() = toString().let(HookType::valueOf)

    override fun toString() = "${symbol.shortName.asString()}Hook"
}

/** Build [HookInfo] from the validated [HookAnnotation] found on the [property] */
internal fun KSPropertyDeclaration.validateHookAnnotation(parentResolver: TypeParameterResolver): ValidatedNel<HookValidationError, HookInfo> =
    onlyHasASingleDslAnnotation().andThen { annotation ->

        val hasCodeGenerator = hasCodeGenerator(annotation)
        val mustBeHookType = mustBeHookType(annotation, parentResolver)
        val validateParameters = validateParameters(annotation, parentResolver)
        val hookMember = simpleName.asString()
        val propertyVisibility = this.getVisibility().toKModifier() ?: KModifier.PUBLIC

        hasCodeGenerator.zip(
            mustBeHookType,
            validateParameters
        ) { hookType: HookType, hookSignature: HookSignature, hookParameters: List<HookParameter> ->
            HookInfo(hookMember, hookType, hookSignature, hookParameters, propertyVisibility)
        }
    }

private fun KSPropertyDeclaration.onlyHasASingleDslAnnotation(): ValidatedNel<HookValidationError, HookAnnotation> {
    val annotations = annotations.filter { it.shortName.asString() in annotationDslMarkers }.toList()
    return when(annotations.size) {
        0 -> HookValidationError.NoHookDslAnnotations(this).invalidNel()
        1 -> annotations.single().let(::HookAnnotation).valid()
        else -> HookValidationError.TooManyHookDslAnnotations(annotations, this).invalidNel()
    }
}

private fun validateParameters(annotation: HookAnnotation, parentResolver: TypeParameterResolver): ValidatedNel<HookValidationError, List<HookParameter>> = try {
    annotation.hookFunctionSignatureReference.functionParameters.mapIndexed { index: Int, parameter: KSValueParameter ->
        val name = parameter.name?.asString()
        val type = parameter.type.toTypeName(parentResolver)
        HookParameter(name, type, index)
    }.valid()
} catch (exception: Exception) {
    HookValidationError.MustBeHookTypeSignature(annotation).invalidNel()
}

private fun hasCodeGenerator(annotation: HookAnnotation): ValidatedNel<HookValidationError, HookType> = try {
    annotation.type.valid()
} catch (e: Exception) {
    HookValidationError.NoCodeGenerator(annotation).invalidNel()
}

private fun mustBeHookType(annotation: HookAnnotation, parentResolver: TypeParameterResolver): ValidatedNel<HookValidationError, HookSignature> = try {
    val isSuspend: Boolean = annotation.hookFunctionSignatureType.modifiers.contains(Modifier.SUSPEND)
    // I'm leaving this here because KSP knows that it's (String) -> Int, whereas once it gets to Poet, it's just kotlin.Function1<kotlin.Int, kotlin.String>
    val text = annotation.hookFunctionSignatureType.text
    val hookFunctionSignatureType = annotation.hookFunctionSignatureType.toTypeName(parentResolver)
    val returnType = annotation.hookFunctionSignatureReference.returnType.toTypeName(parentResolver)
    val returnTypeType = annotation.hookFunctionSignatureReference.returnType.element?.typeArguments?.firstOrNull()?.toTypeName(parentResolver)

    HookSignature(
        text,
        isSuspend,
        returnType,
        returnTypeType,
        hookFunctionSignatureType
    ).valid()
} catch (exception: Exception) {
    HookValidationError.MustBeHookTypeSignature(annotation).invalidNel()
}
