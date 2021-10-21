package com.intuit.hooks

public abstract class AsyncSeriesBailHook<F : Function<BailResult<R>>, R> : AsyncBaseHook<F>("AsyncSeriesBailHook") {
    protected suspend fun call(invokeWithContext: suspend (F, HookContext) -> BailResult<R>): R? {
        val context = setup(invokeWithContext)

        taps.values.forEach { tapInfo ->
            when (val result = invokeWithContext(tapInfo.f, context)) {
                is BailResult.Bail<R> -> return@call result.value
            }
        }

        return null
    }
}
