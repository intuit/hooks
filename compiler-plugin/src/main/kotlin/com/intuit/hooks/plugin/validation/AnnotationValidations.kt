package com.intuit.hooks.plugin.validation

import arrow.core.*
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.codegen.HookMember
import com.intuit.hooks.plugin.codegen.HookParameter
import com.intuit.hooks.plugin.codegen.HookSignature
import com.intuit.hooks.plugin.codegen.HookType
import com.intuit.hooks.plugin.codegen.HookType.Companion.annotationDslMarkers
import com.intuit.hooks.plugin.ksp.HooksProcessor
import com.intuit.hooks.plugin.ksp.text

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
internal fun validateHookAnnotation(
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookInfo> = onlyHasASingleDslAnnotation(property).withEither {
    it.flatMap { annotation ->
        hasCodeGenerator(annotation).zip(
            mustBeHookType(annotation),
            validateParameters(annotation),
            HookMember(
                property.simpleName.asString(),
                property.getVisibility().name.lowercase(),
            ).let(::HookInfo::partially1),
        ).toEither()
    }
}

private fun onlyHasASingleDslAnnotation(property: KSPropertyDeclaration): ValidatedNel<HookValidationError, HookAnnotation> {
    val annotations = property.annotations.filter { it.shortName.asString() in annotationDslMarkers }.toList()
    if (annotations.size != 1) return HookValidationError.MustOnlyHaveSingleDslAnnotation(annotations, property).invalidNel()
    return annotations.single().let(::HookAnnotation).valid()
}

private fun validateParameters(annotation: HookAnnotation): ValidatedNel<HookValidationError, List<HookParameter>> = try {
    annotation.hookFunctionSignatureReference.functionParameters.mapIndexed { index: Int, parameter: KSValueParameter ->
        HookParameter(parameter.name?.asString(), parameter.type.text, index)
    }.valid()
} catch (exception: Exception) {
    HookValidationError.MustBeHookTypeSignature(annotation).invalidNel()
}

private fun hasCodeGenerator(annotation: HookAnnotation): ValidatedNel<HookValidationError, HookType> = try {
    annotation.type.valid()
} catch (e: Exception) {
    HookValidationError.NoCodeGenerator(annotation).invalidNel()
}

private fun mustBeHookType(annotation: HookAnnotation): ValidatedNel<HookValidationError, HookSignature> = try {
    HookSignature(
        annotation.hookFunctionSignatureType.text,
        annotation.hookFunctionSignatureType.modifiers.contains(Modifier.SUSPEND),
        annotation.hookFunctionSignatureReference.returnType.text,
        annotation.hookFunctionSignatureReference.returnType.element?.typeArguments?.firstOrNull()?.text,
    ).valid()
} catch (exception: Exception) {
    HookValidationError.MustBeHookTypeSignature(annotation).invalidNel()
}
