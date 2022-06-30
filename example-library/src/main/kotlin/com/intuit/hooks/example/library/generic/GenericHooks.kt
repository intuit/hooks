package com.intuit.hooks.example.library.generic

import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal abstract class GenericHooks : Hooks() {
    @Sync<(newSpeed: Int) -> Unit> abstract val sync: Hook
    @SyncBail<(Boolean) -> BailResult<Int>> abstract val syncBail: Hook
    @SyncLoop<(foo: Boolean) -> LoopResult> abstract val syncLoop: Hook
    @SyncWaterfall<(name: String) -> String> abstract val syncWaterfall: Hook
    @ExperimentalCoroutinesApi
    @AsyncParallelBail<suspend (String) -> BailResult<String>> abstract val asyncParallelBail: Hook
    @AsyncParallel<suspend (String) -> Int> abstract val asyncParallel: Hook
    @AsyncSeries<suspend (String) -> Int> abstract val asyncSeries: Hook
    @AsyncSeriesBail<suspend (String) -> BailResult<String>> abstract val asyncSeriesBail: Hook
    @AsyncSeriesLoop<suspend (String) -> LoopResult> abstract val asyncSeriesLoop: Hook
    @AsyncSeriesWaterfall<suspend (String) -> String> abstract val asyncSeriesWaterfall: Hook
}
