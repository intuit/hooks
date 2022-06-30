package com.intuit.hooks.plugin.ksp.validation

import arrow.core.*
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.ksp.text
import com.squareup.kotlinpoet.ksp.TypeParameterResolver

// TODO: It'd be nice if the validations were codegen framework agnostic
internal sealed class HookValidationError(val message: String, val symbol: KSNode) {
    class AsyncHookWithoutSuspend(symbol: KSNode) : HookValidationError("Async hooks must be defined with a suspend function signature", symbol)
    class WaterfallMustHaveParameters(symbol: KSNode) : HookValidationError("Waterfall hooks must take at least one parameter", symbol)
    class WaterfallParameterTypeMustMatch(symbol: KSNode) : HookValidationError("Waterfall hooks must specify the same types for the first parameter and the return type", symbol)
    class MustBeHookTypeSignature(annotation: HookAnnotation) : HookValidationError("$annotation property requires a hook type signature", annotation.symbol)
    class NoCodeGenerator(annotation: HookAnnotation) : HookValidationError("This hook plugin has no code generator for $annotation", annotation.symbol)
    class NoHookDslAnnotations(property: KSPropertyDeclaration) : HookValidationError("Hook property must be annotated with a DSL annotation", property)
    class TooManyHookDslAnnotations(annotations: List<KSAnnotation>, property: KSPropertyDeclaration) : HookValidationError("This hook has more than a single hook DSL annotation: $annotations", property)
    class UnsupportedAbstractPropertyType(property: KSPropertyDeclaration) : HookValidationError("Abstract property type (${property.type.text}) not supported. Hook properties must be of type com.intuit.hooks.Hook", property)
    class NotAnAbstractProperty(property: KSPropertyDeclaration) : HookValidationError("Hooks can only be abstract properties", property)
}

/** main entrypoint for validating [KSPropertyDeclaration]s as valid annotated hook members */
internal fun validateProperty(property: KSPropertyDeclaration, parentResolver: TypeParameterResolver): ValidatedNel<HookValidationError, HookInfo> = with(property) {
    // validate property has the correct type
    validateHookType()
        .andThen { validateHookAnnotation(parentResolver) }
        // validate property against hook info with specific hook type validations
        .andThen { info -> validateHookProperties(info) }
}

private fun KSPropertyDeclaration.validateHookType(): ValidatedNel<HookValidationError, KSTypeReference> =
    if (type.text == "Hook") type.valid()
    else HookValidationError.UnsupportedAbstractPropertyType(this).invalidNel()

private fun KSPropertyDeclaration.validateHookProperties(hookInfo: HookInfo) =
    hookInfo.hookType.properties.map { it.validate(hookInfo, this) }
        .sequence()
        .map { hookInfo }
