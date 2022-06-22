package com.intuit.hooks.plugin.ksp.validation

import arrow.core.*
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.codegen.HookType
import com.intuit.hooks.plugin.ksp.text

// TODO: It'd be nice if the validations were codegen framework agnostic
internal sealed class HookValidationError(val message: String, val symbol: KSNode) {
    class AsyncHookWithoutSuspend(symbol: KSNode) : HookValidationError("Async hooks must be defined with a suspend function signature", symbol)
    class WaterfallMustHaveParameters(symbol: KSNode) : HookValidationError("Waterfall hooks must take at least one parameter", symbol)
    class WaterfallParameterTypeMustMatch(symbol: KSNode) : HookValidationError("Waterfall hooks must specify the same types for the first parameter and the return type", symbol)
    class MustBeHookTypeSignature(annotation: HookAnnotation) : HookValidationError("$annotation property requires a hook type signature", annotation.symbol)
    class NoCodeGenerator(annotation: HookAnnotation) : HookValidationError("This hook plugin has no code generator for $annotation", annotation.symbol)
    class NoHookDslAnnotations(property: KSPropertyDeclaration) : HookValidationError("Hook property must be annotated with respective DSL annotation for ${property.type.text}", property)
    class TooManyHookDslAnnotations(annotations: List<KSAnnotation>, property: KSPropertyDeclaration) : HookValidationError("This hook has more than a single hook DSL annotation: $annotations", property)
    class HookPropertyTypeMismatch(property: KSPropertyDeclaration, annotationType: String) : HookValidationError("Hook property type (${property.type.text}) does not match annotation hook type (@${annotationType.dropLast(4)})", property)
    class UnsupportedAbstractPropertyType(property: KSPropertyDeclaration) : HookValidationError("Abstract property type (${property.type.text}) not supported", property)
}

/** main entrypoint for validating [KSPropertyDeclaration]s as valid annotated hook members */
internal fun validateProperty(property: KSPropertyDeclaration): ValidatedNel<HookValidationError, HookInfo> = with(property) {
    // validate property has the correct type
    validateHookType().withEither { either ->
        either.flatMap { type ->
            // aggregate property hook type with annotation hook info
            type.valid().zip(
                validateHookAnnotation(),
                ::Pair
            ).toEither()
        }
    }.withEither {
        it.flatMap { (type, info) ->
            // validate property against hook info with specific hook type validations
            validatePropertyTypeAgainstHookInfo(type, info).zip(
                validateHookProperties(info)
            ) { _, _ -> info }.toEither()
        }
    }
}

private fun KSPropertyDeclaration.validateHookType(): ValidatedNel<HookValidationError, HookType> = try {
    HookType.valueOf(type.element.toString()).valid()
} catch (e: Exception) {
    HookValidationError.UnsupportedAbstractPropertyType(this).invalidNel()
}

private fun KSPropertyDeclaration.validatePropertyTypeAgainstHookInfo(
    type: HookType,
    info: HookInfo
): ValidatedNel<HookValidationError.HookPropertyTypeMismatch, HookInfo> =
    if (info.hookType == type) info.valid()
    else HookValidationError.HookPropertyTypeMismatch(this, info.hookType.name).invalidNel()

private fun KSPropertyDeclaration.validateHookProperties(hookInfo: HookInfo) =
    hookInfo.hookType.properties.map { it.validate(hookInfo, this) }
        .sequence()
        .map { hookInfo }
