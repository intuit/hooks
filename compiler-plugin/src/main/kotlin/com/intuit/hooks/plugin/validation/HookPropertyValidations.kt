package com.intuit.hooks.plugin.validation

import arrow.core.ValidatedNel
import arrow.core.invalidNel
import arrow.core.valid
import arrow.core.zip
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.codegen.HookProperty

internal fun HookProperty.validate(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> = when (this) {
    is HookProperty.Bail -> valid()
    is HookProperty.Loop -> valid()
    is HookProperty.Async -> validate(info, property)
    is HookProperty.Waterfall -> validate(info, property)
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
    arity(info, property).zip(
        parameters(info, property),
    ) { _, _ -> this }

private fun HookProperty.Waterfall.arity(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> {
    return if (!info.zeroArity) valid()
    else HookValidationError.WaterfallMustHaveParameters(property).invalidNel()
}

private fun HookProperty.Waterfall.parameters(
    info: HookInfo,
    property: KSPropertyDeclaration,
): ValidatedNel<HookValidationError, HookProperty> {
    return if (info.hookSignature.returnType == info.params.firstOrNull()?.type) valid()
    else HookValidationError.WaterfallParameterTypeMustMatch(property).invalidNel()
}
