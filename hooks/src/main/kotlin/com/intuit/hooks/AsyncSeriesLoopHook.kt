package com.intuit.hooks

public abstract class AsyncSeriesLoopHook<F : Function<LoopResult>, FInterceptor : Function<*>> : AsyncBaseHook<F>("AsyncSeriesLoopHook") {
    override val interceptors: LoopInterceptors<F, FInterceptor> = LoopInterceptors()

    protected suspend fun call(
        invokeTap: suspend (F, HookContext) -> LoopResult,
        invokeInterceptor: suspend (FInterceptor, HookContext) -> Unit
    ) {
        val context = setup(invokeTap, runTapInterceptors = false)

        do {
            interceptors.invokeTapInterceptors(taps.values, context)
            interceptors.loop.forEach { interceptor ->
                invokeInterceptor(interceptor, context)
            }

            val restartFound = taps.values.find { tapInfo ->
                val result = invokeTap(tapInfo.f, context)
                result == LoopResult.Restart
            }
        } while (restartFound != null)
    }

    public fun interceptLoop(f: FInterceptor) {
        interceptors.loop.add(f)
    }
}
