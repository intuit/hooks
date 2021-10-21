package com.intuit.hooks

public abstract class AsyncSeriesHook<F : Function<*>> : AsyncBaseHook<F>("AsyncSeriesHook") {
    protected suspend fun call(invokeWithContext: suspend (F, HookContext) -> Unit) {
        val context = setup(invokeWithContext)
        return taps.forEach { tapInfo -> invokeWithContext(tapInfo.f, context) }
    }
}
