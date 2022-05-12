package com.intuit.hooks.plugin.validation

import arrow.core.*
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookInfo

// TODO: It'd be nice if the validations were compiler plugin framework agnostic
internal sealed class HookValidationError(val message: String, val symbol: KSNode) {
    class AsyncHookWithoutSuspend(symbol: KSNode) : HookValidationError("Async hooks must be defined with a suspend function signature", symbol)
    class WaterfallMustHaveParameters(symbol: KSNode) : HookValidationError("Waterfall hooks must take at least one parameter", symbol)
    class WaterfallParameterTypeMustMatch(symbol: KSNode) : HookValidationError("Waterfall hooks must specify the same types for the first parameter and the return type", symbol)
    class MustBeHookTypeSignature(annotation: HookAnnotation) : HookValidationError("$annotation property requires a hook type signature", annotation.symbol)
    class NoCodeGenerator(annotation: HookAnnotation) : HookValidationError("This hook plugin has no code generator for $annotation", annotation.symbol)
    class MustOnlyHaveSingleDslAnnotation(annotations: List<KSAnnotation>, property: KSPropertyDeclaration) : HookValidationError("This hook has more than a single hook DSL annotation: $annotations", property)
}

/** main entrypoint for validating [KSPropertyDeclaration]s as valid annotated hook members */
internal fun validateHook(property: KSPropertyDeclaration): Validated<NonEmptyList<HookValidationError>, HookInfo> =
    validateHookAnnotation(property).withEither {
        it.flatMap { hookInfo -> validateHookProperties(property, hookInfo).toEither() }
    }

private fun validateHookProperties(property: KSPropertyDeclaration, hookInfo: HookInfo) =
    hookInfo.hookType.properties.map { it.validate(hookInfo, property) }
        .sequenceValidated()
        .map { hookInfo }
