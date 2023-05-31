package com.intuit.hooks

public sealed class BailResult<T> {
    public class Bail<T>(public val value: T) : BailResult<T>()

    public class Continue<T> : BailResult<T>()
}

public abstract class SyncBailHook<T, F : (HookContext, T) -> BailResult<R>, R> : SyncBaseHook<F>("SyncBailHook") {
    protected fun call(invokeWithContext: (F, HookContext) -> BailResult<R>, default: ((T) -> Unit)? = null, p1: T): R? {
        val context = setup(invokeWithContext)

        taps.forEach { tapInfo ->
            when (val result = invokeWithContext(tapInfo.f, context)) {
                is BailResult.Bail<R> -> return@call result.value
                is BailResult.Continue -> {}
            }
        }

        default?.invoke(p1)
        return null
    }
}
