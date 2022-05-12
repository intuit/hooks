package com.intuit.hooks.example.library.generic

import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal abstract class GenericHooks : Hooks() {
    @Sync<(newSpeed: Int) -> Unit>() abstract val sync: SyncHook<*>
    @SyncBail<(Boolean) -> BailResult<Int>>() abstract val syncBail: SyncBailHook<*, *>
    @SyncLoop<(foo: Boolean) -> LoopResult>() abstract val syncLoop: SyncLoopHook<*, *>
    @SyncWaterfall<(name: String) -> String>() abstract val syncWaterfall: SyncWaterfallHook<*, *>
    @ExperimentalCoroutinesApi
    @AsyncParallelBail<suspend (String) -> BailResult<String>>() abstract val asyncParallelBail: AsyncParallelBailHook<*, *>
    @AsyncParallel<suspend (String) -> Int>() abstract val asyncParallel: AsyncParallelHook<*>
    @AsyncSeries<suspend (String) -> Int>() abstract val asyncSeries: AsyncSeriesHook<*>
    @AsyncSeriesBail<suspend (String) -> BailResult<String>>() abstract val asyncSeriesBail: AsyncSeriesBailHook<*, *>
    @AsyncSeriesLoop<suspend (String) -> LoopResult>() abstract val asyncSeriesLoop: AsyncSeriesLoopHook<*, *>
    @AsyncSeriesWaterfall<suspend (String) -> String>() abstract val asyncSeriesWaterfall: AsyncSeriesWaterfallHook<*, *>
}
