package com.intuit.hooks.plugin.ksp.validation

import arrow.core.*
import arrow.core.raise.*
import arrow.core.raise.ensure
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.ksp.text
import com.intuit.hooks.plugin.ksp.validation.ensure
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

//context(HookValidationError)
internal fun KSPLogger.error(validationError: HookValidationError) {
    error(validationError.message, validationError.symbol)
}

internal sealed interface LogicalFailure

/** Logical failure that can be ignored */
internal sealed interface EdgeCase : LogicalFailure {
    class NoHooksDefined(val file: KSFile) : EdgeCase
}

/** Logical failure that should probably be reported */
internal sealed interface ErrorCase : LogicalFailure {
    val message: String
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
    class UnsupportedAbstractPropertyType(property: KSPropertyDeclaration) : HookValidationError("Abstract property type (${property.type.text}) not supported. Hook properties must be of type com.intuit.hooks.Hook", property)
    class NotAnAbstractProperty(property: KSPropertyDeclaration) : HookValidationError("Hooks can only be abstract properties", property)

    operator fun component1(): String = message

    operator fun component2(): KSNode = symbol
}

context(Raise<Nel<HookValidationError>>)
internal fun KSPropertyDeclaration.validateProperty(parentResolver: TypeParameterResolver): HookInfo {
    // 1. validate types
    // 2. validation annotation and
    // 3. validate properties against type

    // why is validateHookType wrapped in ensure while nothing else is?
    // great question! this is because validateHookType is a singularly
    // concerned validation function that has an explicitly matching
    // raise context. validateHookType will _only_ ever raise a singular
    // error, and therefore, shouldn't be treated as if it might have
    // many to raise. We use ensure to narrow down the raise type param
    // to what we expect, and then unwrap to explicitly re-raise within
    // a non-empty-list context.

    ensure {
        validateHookType()
    }

    return validateHookAnnotation(parentResolver).also {
        validateHookProperties(it)
    }
}

/** main entrypoint for validating [KSPropertyDeclaration]s as valid annotated hook members */
//internal fun validateProperty(property: KSPropertyDeclaration, parentResolver: TypeParameterResolver): ValidatedNel<HookValidationError, HookInfo> = with(property) {
//    // validate property has the correct type
//    validateHookType()
//        .andThen { validateHookAnnotation(parentResolver) }
//        // validate property against hook info with specific hook type validations
//        .andThen { info -> validateHookProperties(info) }
//
//    recover { validateHookType() }
//        .map { validateHookProperties(parentResolver) }
//        .map { info -> validateHookProperties(info) }
//
//    fold(
//        { validateHookType() },
//    )
//}

context(Raise<HookValidationError.UnsupportedAbstractPropertyType>)
private fun KSPropertyDeclaration.validateHookType() {
    ensure(type.text == "Hook") {
        HookValidationError.UnsupportedAbstractPropertyType(this)
    }
}

private fun KSPropertyDeclaration.validateHookType(): ValidatedNel<HookValidationError, KSTypeReference> =
    if (type.text == "Hook") type.valid()
    else HookValidationError.UnsupportedAbstractPropertyType(this).invalidNel()


context(Raise<Nel<HookValidationError>>) private fun KSPropertyDeclaration.validateHookProperties(info: HookInfo) {
    info.hookType.properties.map {
        it.validate(info, this)
    }
}

//private fun KSPropertyDeclaration.validateHookProperties(hookInfo: HookInfo): Validated<NonEmptyList<HookValidationError>, HookInfo> =
//    hookInfo.hookType.properties.map { it.validate(hookInfo, this) }
//        .sequence()
//        .map { hookInfo }

/** Helper for accumulating errors from single-error validators */
@RaiseDSL
@OptIn(ExperimentalTypeInference::class)
internal fun <Error, A> Raise<Nel<Error>>.ensure(@BuilderInference block: Raise<Error>.() -> A): A =
    recover(block) { e: Error -> raise(e.nel()) }

/** Helper for accumulating errors from single-error validators */
@RaiseDSL
public inline fun <Error> Raise<Nel<Error>>.ensure(condition: Boolean, raise: () -> Error) {
    recover({ ensure(condition, raise) }) { e: Error -> raise(e.nel()) }
}

/** Raise a _logical failure_ of type [Error] */
@RaiseDSL
public inline fun <Error> Raise<Nel<Error>>.raise(r: Error): Nothing {
    raise(r.nel())
}
