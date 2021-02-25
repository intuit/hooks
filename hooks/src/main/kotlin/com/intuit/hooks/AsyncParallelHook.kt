package com.intuit.hooks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public abstract class AsyncParallelHook<F : Function<*>> : AsyncBaseHook<F>("AsyncParallelHook") {
    protected suspend fun call(scope: CoroutineScope, invokeWithContext: suspend (F, HookContext) -> Unit) {
        val context = setup(invokeWithContext)

        return taps.forEach { tapInfo ->
            scope.launch {
                invokeWithContext(tapInfo.f, context)
            }
        }
    }
}
