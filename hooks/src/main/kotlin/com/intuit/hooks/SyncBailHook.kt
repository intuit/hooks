package com.intuit.hooks

public sealed class BailResult<T> {
    public class Bail<T>(public val value: T) : BailResult<T>()

    public class Continue<T> : BailResult<T>()
}

public abstract class SyncBailHook<F : Function<BailResult<R>>, R> : SyncBaseHook<F>("SyncBailHook") {
    protected fun call(invokeWithContext: (F, HookContext) -> BailResult<R>): R? {
        val context = setup(invokeWithContext)

        taps.forEach { tapInfo ->
            when (val result = invokeWithContext(tapInfo.f, context)) {
                is BailResult.Bail<R> -> return@call result.value
            }
        }

        return null
    }
}
