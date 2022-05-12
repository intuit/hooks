package com.intuit.hooks.plugin.validation

import arrow.core.*
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookCodeGen
import com.intuit.hooks.plugin.codegen.HookParameter
import com.intuit.hooks.plugin.codegen.HookSignature
import com.intuit.hooks.plugin.codegen.HookType
import com.intuit.hooks.plugin.ksp.HookAnnotation
import com.intuit.hooks.plugin.ksp.HookClassInfo
import com.intuit.hooks.plugin.ksp.text

// TODO: It'd be nice if the validations were compiler plugin framework agnostic
internal sealed class HookValidationError(val message: String, var symbol: KSNode? = null) {
    class AsyncHookWithoutSuspend(hookClassInfo: HookClassInfo) : HookValidationError("Async hooks must be defined with a suspend function signature", hookClassInfo.property)
    class WaterfallMustHaveParameters(hookClassInfo: HookClassInfo) : HookValidationError("Waterfall hooks must take at least one parameter", hookClassInfo.property)
    class WaterfallParameterTypeMustMatch(hookClassInfo: HookClassInfo) : HookValidationError("Waterfall hooks must specify the same types for the first parameter and the return type", hookClassInfo.property)
    class MustBeHookTypeSignature(annotation: HookAnnotation) : HookValidationError("$annotation property requires a hook type signature", annotation.symbol)
    class NoCodeGenerator(annotation: HookAnnotation) : HookValidationError("This hook plugin has no code generator for $annotation", annotation.symbol)
    class MustOnlyHaveSingleDslAnnotation(annotations: List<KSAnnotation>, property: KSPropertyDeclaration) : HookValidationError("This hook has more than a single hook DSL annotation: $annotations", property)
}

internal fun validateHook(property: KSPropertyDeclaration): Validated<NonEmptyList<HookValidationError>, HookCodeGen> =
    validateHookType(property).withEither { e ->
        e.flatMap { h -> validateHookProperties(h).toEither() }
    }.mapLeft { invalid ->
        // Attach property to validations that don't have a symbol attached
        invalid.onEach {
            it.symbol = it.symbol ?: property
        }
    }

private fun validateHookProperties(hook: HookClassInfo) =
    hook.hookType.properties.map { it.validate(hook) }
        .sequenceValidated()
        .map { hook.toCodeGen() }

internal val annotationDslMarkers = listOf(
    "Sync",
    "SyncBail",
    "SyncWaterfall",
    "SyncLoop",
    "AsyncParallel",
    "AsyncParallelBail",
    "AsyncSeries",
    "AsyncSeriesBail",
    "AsyncSeriesWaterfall",
    "AsyncSeriesLoop",
)

private fun validateHookType(property: KSPropertyDeclaration): ValidatedNel<HookValidationError, HookClassInfo> = onlyHasASingleDslAnnotation(property).withEither {
    val hookClassInfo = ::HookClassInfo.partially1(property)
    it.flatMap { annotation ->
        validateHookAnnotation(annotation, hookClassInfo).toEither()
    }
}

private fun validateHookAnnotation(annotation: HookAnnotation, factory: (HookSignature, HookType, List<HookParameter>) -> HookClassInfo): ValidatedNel<HookValidationError, HookClassInfo> = mustBeHookType(annotation).zip(
    hasCodeGenerator(annotation),
    validateParameters(annotation),
    factory,
)

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


private fun onlyHasASingleDslAnnotation(property: KSPropertyDeclaration): ValidatedNel<HookValidationError, HookAnnotation> {
    val annotations = property.annotations.filter { it.shortName.asString() in annotationDslMarkers }.toList()
    if (annotations.size != 1) return HookValidationError.MustOnlyHaveSingleDslAnnotation(annotations, property).invalidNel()
    return annotations.single().let(::HookAnnotation).valid()
}