package com.intuit.hooks

public abstract class SyncHook<F : Function<*>> : SyncBaseHook<F>("SyncHook") {
    protected fun call(invokeWithContext: (F, HookContext) -> Unit) {
        val context = setup(invokeWithContext)
        return taps.forEach { tapInfo -> invokeWithContext(tapInfo.f, context) }
    }
}
