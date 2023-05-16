package com.intuit.hooks.plugin.ksp.validation

import arrow.core.*
import arrow.core.raise.*
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.codegen.HookProperty
import kotlin.contracts.CallsInPlace
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

internal fun HookProperty.validate(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> = when (this) {
    is HookProperty.Bail -> valid()
    is HookProperty.Loop -> valid()
    is HookProperty.Async -> validate(info, property)
    is HookProperty.Waterfall -> validate(info, property)
}

context(Raise<Nel<HookValidationError>>)
internal fun HookProperty.validate(
    info: HookInfo,
    property: KSPropertyDeclaration,
) {
    when (this) {
        is HookProperty.Bail -> Unit
        is HookProperty.Loop -> Unit
        is HookProperty.Async -> raiseSingle {
            info.validateAsync(property)
        }
        is HookProperty.Waterfall -> validate(info, property)
    }
}

context(Raise<HookValidationError.AsyncHookWithoutSuspend>)
private fun HookInfo.validateAsync(property: KSPropertyDeclaration) {
    ensure(hookSignature.isSuspend) { HookValidationError.AsyncHookWithoutSuspend(property) }
}

private fun HookProperty.Async.validate(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> =
    if (info.hookSignature.isSuspend) valid()
    else HookValidationError.AsyncHookWithoutSuspend(property).invalidNel()

private fun HookProperty.Waterfall.validate(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> =
    Either.zipOrAccumulate(
        arity(info, property).toEither(),
        parameters(info, property).toEither()
    ) { _, _ -> this }.toValidated()

context(Raise<Nel<HookValidationError>>)
private fun HookProperty.Waterfall.validate(
    info: HookInfo,
    property: KSPropertyDeclaration,
) {
    zipOrAccumulate(
        { arity(info, property) },
        { parameters(info, property) },
    ) { _, _ -> }
}

private fun HookProperty.Waterfall.arity(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> {
    return if (!info.zeroArity) valid()
    else HookValidationError.WaterfallMustHaveParameters(property).invalidNel()
}

context(Raise<HookValidationError.WaterfallMustHaveParameters>)
private fun HookProperty.Waterfall.arity(
    info: HookInfo,
    property: KSPropertyDeclaration,
) {
    ensure(!info.zeroArity) { HookValidationError.WaterfallMustHaveParameters(property) }
}


private fun HookProperty.Waterfall.parameters(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> {
    return if (info.hookSignature.returnType == info.params.firstOrNull()?.type) valid()
    else HookValidationError.WaterfallParameterTypeMustMatch(property).invalidNel()
}

context(Raise<HookValidationError.WaterfallParameterTypeMustMatch>)
private fun HookProperty.Waterfall.parameters(
    info: HookInfo,
    property: KSPropertyDeclaration,
) {
    ensure(info.hookSignature.returnType == info.params.firstOrNull()?.type) {
        HookValidationError.WaterfallParameterTypeMustMatch(property)
    }
}
