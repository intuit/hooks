package com.intuit.hooks.plugin.ksp.validation

import arrow.core.*
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
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

/** Wrapper for [KSAnnotation] when we're sure that the annotation is a hook annotation */
@JvmInline internal value class HookAnnotation(val symbol: KSAnnotation) {
    val hookFunctionSignatureType get() = symbol.annotationType.element?.typeArguments?.single()?.type
        ?: throw HooksProcessor.Exception("Could not determine hook function signature type for $symbol")

    val hookFunctionSignatureReference get() = hookFunctionSignatureType.element as? KSCallableReference
        ?: throw HooksProcessor.Exception("Hook type argument must be a function for $symbol")

    // NOTE: THIS IS AMAZING - can provide typical nullable APIs for consumers who don't care about working with the explicit typed errors
    val type get() = recover({ type }, { null })

    // TODO: Maybe put in smart constructor, but this is so cool to be able to provide
    //       an alternative API for those who would prefer raise over exceptions
    context(Raise<HookValidationError.NoCodeGenerator>) val type: HookType get() {
        ensure(toString() in HookType.supportedHookTypes) {
            HookValidationError.NoCodeGenerator(this)
        }

        return HookType.valueOf(toString())
    }

    override fun toString() = "${symbol.shortName.asString()}Hook"
}

context(Raise<Nel<HookValidationError>>)
internal fun KSPropertyDeclaration.validateHookAnnotation(parentResolver: TypeParameterResolver): HookInfo {
    val annotation = ensure { onlyHasASingleDslAnnotation() }

    return zipOrAccumulate(
        { simpleName.asString() },
        { annotation.hasCodeGenerator() },
        { annotation.mustBeHookType(parentResolver) },
        { annotation.validateParameters(parentResolver) },
        { getVisibility().toKModifier() ?: KModifier.PUBLIC },
        ::HookInfo
    )
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

// TODO: This'd be a good smart constructor use case
context(Raise<HookValidationError>) private fun KSPropertyDeclaration.onlyHasASingleDslAnnotation(): HookAnnotation {
    val annotations = annotations.filter { it.shortName.asString() in annotationDslMarkers }.toList()
    return when (annotations.size) {
        0 -> raise(HookValidationError.NoHookDslAnnotations(this))
        1 -> annotations.single()
        else -> raise(HookValidationError.TooManyHookDslAnnotations(annotations, this))
    }.let(::HookAnnotation)
}

private fun KSPropertyDeclaration.onlyHasASingleDslAnnotation(): ValidatedNel<HookValidationError, HookAnnotation> {
    val annotations = annotations.filter { it.shortName.asString() in annotationDslMarkers }.toList()
    return when (annotations.size) {
        0 -> HookValidationError.NoHookDslAnnotations(this).invalidNel()
        1 -> annotations.single().let(::HookAnnotation).valid()
        else -> HookValidationError.TooManyHookDslAnnotations(annotations, this).invalidNel()
    }
}

context(Raise<HookValidationError>) private fun HookAnnotation.validateParameters(parentResolver: TypeParameterResolver): List<HookParameter> = try {
    hookFunctionSignatureReference.functionParameters.mapIndexed { index: Int, parameter: KSValueParameter ->
        val name = parameter.name?.asString()
        val type = parameter.type.toTypeName(parentResolver)
        HookParameter(name, type, index)
    }
} catch (exception: Exception) {
    raise(HookValidationError.MustBeHookTypeSignature(this))
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

// TODO: This would be obsolete with smart constructor
context(Raise<HookValidationError.NoCodeGenerator>) private fun HookAnnotation.hasCodeGenerator(): HookType = type

private fun hasCodeGenerator(annotation: HookAnnotation): ValidatedNel<HookValidationError, HookType> = try {
    annotation.type!!.valid()
} catch (e: Exception) {
    HookValidationError.NoCodeGenerator(annotation).invalidNel()
}

/** TODO: Another good smart constructor example */
context(Raise<HookValidationError>)
private fun HookAnnotation.mustBeHookType(parentResolver: TypeParameterResolver): HookSignature = try {
    val isSuspend: Boolean = hookFunctionSignatureType.modifiers.contains(Modifier.SUSPEND)
    // I'm leaving this here because KSP knows that it's (String) -> Int, whereas once it gets to Poet, it's just kotlin.Function1<kotlin.Int, kotlin.String>
    val text = hookFunctionSignatureType.text
    val hookFunctionSignatureType = hookFunctionSignatureType.toTypeName(parentResolver)
    val returnType = hookFunctionSignatureReference.returnType.toTypeName(parentResolver)
    val returnTypeType = hookFunctionSignatureReference.returnType.element?.typeArguments?.firstOrNull()?.toTypeName(parentResolver)

    HookSignature(
        text,
        isSuspend,
        returnType,
        returnTypeType,
        hookFunctionSignatureType
    )
} catch (exception: Exception) {
    raise(HookValidationError.MustBeHookTypeSignature(this))
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
