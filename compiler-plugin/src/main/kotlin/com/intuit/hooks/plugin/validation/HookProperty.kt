package com.intuit.hooks.plugin.validation

import arrow.core.ValidatedNel
import arrow.core.invalidNel
import arrow.core.valid
import arrow.core.zip
import com.intuit.hooks.plugin.ksp.HookClassInfo

internal enum class HookProperty {
    Bail,
    Loop,
    Async {
        override fun validate(hookClassInfo: HookClassInfo): ValidatedNel<HookValidationError, HookProperty> {
            return if (hookClassInfo.hookSignature.isSuspend) this.valid()
            else HookValidationError.AsyncHookWithoutSuspend(hookClassInfo).invalidNel()
        }
    },
    Waterfall {
        override fun validate(hookClassInfo: HookClassInfo): ValidatedNel<HookValidationError, HookProperty> {
            return arity(hookClassInfo).zip(
                parameters(hookClassInfo),
            ) { _, _ -> this }
        }

        private fun arity(hookClassInfo: HookClassInfo): ValidatedNel<HookValidationError, HookProperty> {
            return if (!hookClassInfo.zeroArity) this.valid()
            else HookValidationError.WaterfallMustHaveParameters(hookClassInfo).invalidNel()
        }

        private fun parameters(hookClassInfo: HookClassInfo): ValidatedNel<HookValidationError, HookProperty> {
            return if (hookClassInfo.hookSignature.returnType == hookClassInfo.params.firstOrNull()?.type) this.valid()
            else HookValidationError.WaterfallParameterTypeMustMatch(hookClassInfo).invalidNel()
        }
    };

    open fun validate(hookClassInfo: HookClassInfo): ValidatedNel<HookValidationError, HookProperty> = this.valid()
}
