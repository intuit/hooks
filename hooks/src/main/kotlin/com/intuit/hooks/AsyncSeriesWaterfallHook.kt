package com.intuit.hooks

public abstract class AsyncSeriesWaterfallHook<F : Function<*>, R> : AsyncBaseHook<F>("AsyncSeriesWaterfallHook") {
    protected suspend fun call(initial: R, invokeTap: suspend (F, R, HookContext) -> R, invokeInterceptor: suspend (F, HookContext) -> Unit): R {
        val context = setup(invokeInterceptor)
        return taps.values.fold(initial) { r, tapInfo -> invokeTap(tapInfo.f, r, context) }
    }
}
