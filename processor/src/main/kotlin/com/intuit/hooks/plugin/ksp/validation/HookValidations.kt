package com.intuit.hooks.plugin.ksp.validation

import arrow.core.Nel
import arrow.core.mapOrAccumulate
import arrow.core.raise.Raise
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.ensure
import com.intuit.hooks.plugin.ksp.text
import com.intuit.hooks.plugin.mapOrAccumulate
import com.squareup.kotlinpoet.ksp.TypeParameterResolver

context(HookValidationError)
internal fun KSPLogger.error() {
    error(message, symbol)
}

// TODO: It'd be nice if the validations were codegen framework agnostic
internal sealed class HookValidationError(override val message: String, val symbol: KSNode) : ErrorCase {
    class AsyncHookWithoutSuspend(symbol: KSNode) : HookValidationError("Async hooks must be defined with a suspend function signature", symbol)
    class WaterfallMustHaveParameters(symbol: KSNode) : HookValidationError("Waterfall hooks must take at least one parameter", symbol)
    class WaterfallParameterTypeMustMatch(symbol: KSNode) : HookValidationError("Waterfall hooks must specify the same types for the first parameter and the return type", symbol)
    class MustBeHookTypeSignature(annotation: HookAnnotation) : HookValidationError("$annotation property requires a hook type signature", annotation.symbol)
    class NoCodeGenerator(annotation: HookAnnotation) : HookValidationError("This hook plugin has no code generator for $annotation", annotation.symbol)
    class NoHookDslAnnotations(property: KSPropertyDeclaration) : HookValidationError("Hook property must be annotated with a DSL annotation", property)
    class TooManyHookDslAnnotations(annotations: List<KSAnnotation>, property: KSPropertyDeclaration) : HookValidationError("This hook has more than a single hook DSL annotation: $annotations", property)
    class UnsupportedPropertyType(property: KSPropertyDeclaration) : HookValidationError("Property type (${property.type.text}) not supported. Hook properties must be of type com.intuit.hooks.Hook", property)
    class NotAnAbstractProperty(property: KSPropertyDeclaration) : HookValidationError("Hooks can only be abstract properties", property)
    class UnsupportedContainer(declaration: KSClassDeclaration) : HookValidationError("Hooks in constructs other than class, interface, and object aren't supported", declaration)

    operator fun component1(): String = message

    operator fun component2(): KSNode = symbol
}

/** main entrypoint for validating [KSPropertyDeclaration]s as valid annotated hook members */
context(Raise<Nel<HookValidationError>>)
internal fun KSPropertyDeclaration.validateProperty(parentResolver: TypeParameterResolver): HookInfo {
    // 1. validate types
    validateHookType()

    // 2. validation annotation and
    val info = validateHookAnnotation(parentResolver)

    // 3. validate properties against type
    validateHookProperties(info)

    return info
}

context(Raise<Nel<HookValidationError>>)
private fun KSPropertyDeclaration.validateHookType() {
    zipOrAccumulate(
        { ensure(type.text == "Hook") { HookValidationError.UnsupportedPropertyType(this@validateHookType) } },
        { ensure(modifiers.contains(Modifier.ABSTRACT)) { HookValidationError.NotAnAbstractProperty(this@validateHookType) } },
    ) { _, _ -> }
}

context(Raise<Nel<HookValidationError>>) private fun KSPropertyDeclaration.validateHookProperties(info: HookInfo) {
    info.hookType.properties.mapOrAccumulate {
        it.validate(info, this@validateHookProperties)
    }
}
