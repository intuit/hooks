package com.intuit.hooks

public abstract class SyncWaterfallHook<F : Function<*>, R> : SyncBaseHook<F>("SyncWaterfallHook") {
    protected fun call(initial: R, invokeTap: (F, R, HookContext) -> R, invokeInterceptor: (F, HookContext) -> Unit): R {
        val context = setup(invokeInterceptor)
        return taps.fold(initial) { r, tapInfo -> invokeTap(tapInfo.f, r, context) }
    }
}
