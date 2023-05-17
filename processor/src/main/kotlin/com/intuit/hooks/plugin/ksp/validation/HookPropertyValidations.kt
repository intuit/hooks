package com.intuit.hooks.plugin.ksp.validation

import arrow.core.*
import arrow.core.raise.*
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.intuit.hooks.plugin.codegen.HookInfo
import com.intuit.hooks.plugin.codegen.HookProperty
import com.intuit.hooks.plugin.ensure

context(Raise<Nel<HookValidationError>>)
internal fun HookProperty.validate(
    info: HookInfo,
    property: KSPropertyDeclaration,
) {
    when (this) {
        is HookProperty.Bail -> Unit
        is HookProperty.Loop -> Unit
        is HookProperty.Async -> ensure {
            info.validateAsync(property)
        }
        is HookProperty.Waterfall -> validate(info, property)
    }
}

context(Raise<HookValidationError.AsyncHookWithoutSuspend>)
private fun HookInfo.validateAsync(property: KSPropertyDeclaration) {
    ensure(hookSignature.isSuspend) { HookValidationError.AsyncHookWithoutSuspend(property) }
}

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

context(Raise<HookValidationError.WaterfallMustHaveParameters>)
private fun HookProperty.Waterfall.arity(
    info: HookInfo,
    property: KSPropertyDeclaration,
) {
    ensure(!info.zeroArity) { HookValidationError.WaterfallMustHaveParameters(property) }
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
