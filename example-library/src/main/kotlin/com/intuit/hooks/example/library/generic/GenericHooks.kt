package com.intuit.hooks.example.library.generic

import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks
import com.intuit.hooks.dsl.Hooks.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal interface GenericHooks : Hooks {
    @Sync<(newSpeed: Int) -> Unit> val sync: Hook
    @SyncBail<(Boolean) -> BailResult<Int>> val syncBail: Hook
    @SyncLoop<(foo: Boolean) -> LoopResult> val syncLoop: Hook
    @SyncWaterfall<(name: String) -> String> val syncWaterfall: Hook
    @ExperimentalCoroutinesApi
    @AsyncParallelBail<suspend (String) -> BailResult<String>> val asyncParallelBail: Hook
    @AsyncParallel<suspend (String) -> Int> val asyncParallel: Hook
    @AsyncSeries<suspend (String) -> Int> val asyncSeries: Hook
    @AsyncSeriesBail<suspend (String) -> BailResult<String>> val asyncSeriesBail: Hook
    @AsyncSeriesLoop<suspend (String) -> LoopResult> val asyncSeriesLoop: Hook
    @AsyncSeriesWaterfall<suspend (String) -> String> val asyncSeriesWaterfall: Hook
}
