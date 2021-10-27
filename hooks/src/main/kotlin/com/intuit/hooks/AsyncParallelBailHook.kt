package com.intuit.hooks

import com.intuit.hooks.utils.Parallelism.parallelMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull

@ExperimentalCoroutinesApi
public abstract class AsyncParallelBailHook<F : Function<BailResult<R>>, R> : AsyncBaseHook<F>("AsyncParallelBailHook") {
    protected suspend fun call(concurrency: Int, invokeWithContext: suspend (F, HookContext) -> BailResult<R>): R? {
        val context = setup(invokeWithContext)
        return taps.asFlow()
            .parallelMap(concurrency) { invokeWithContext(it.f, context) }
            .filterIsInstance<BailResult.Bail<R>>()
            .firstOrNull()?.value
    }
}
