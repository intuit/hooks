package com.intuit.hooks

public abstract class AsyncSeriesBailHook<F : Function<BailResult<R>>, R> : AsyncBaseHook<F>("AsyncSeriesBailHook") {
    protected suspend fun call(invokeWithContext: suspend (F, HookContext) -> BailResult<R>, default: (suspend (HookContext) -> R)? = null): R? {
        val context = setup(invokeWithContext)

        taps.forEach { tapInfo ->
            when (val result = invokeWithContext(tapInfo.f, context)) {
                is BailResult.Bail<R> -> return@call result.value
                is BailResult.Continue -> {}
            }
        }

        return default?.invoke(context)
    }
}
