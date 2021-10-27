package com.intuit.hooks

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

public abstract class AsyncParallelHook<F : Function<*>> : AsyncBaseHook<F>("AsyncParallelHook") {
    protected suspend fun call(invokeWithContext: suspend (F, HookContext) -> Unit) {
        val context = setup(invokeWithContext)

        coroutineScope {
            taps.forEach { tapInfo ->
                launch {
                    invokeWithContext(tapInfo.f, context)
                }
            }
        }
    }
}
