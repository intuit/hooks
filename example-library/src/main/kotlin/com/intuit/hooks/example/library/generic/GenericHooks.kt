package com.intuit.hooks.example.library.generic

import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks
import kotlinx.coroutines.ExperimentalCoroutinesApi

abstract class GenericHooks : Hooks() {
    open val sync = syncHook<(newSpeed: Int) -> Unit>()
    open val syncBail = syncBailHook<(Boolean) -> BailResult<Int>>()
    open val syncLoop = syncLoopHook<(foo: Boolean) -> LoopResult>()
    open val syncWaterfall = syncWaterfallHook<(name: String) -> String>()
    @ExperimentalCoroutinesApi
    open val asyncParallelBail = asyncParallelBailHook<suspend (String) -> BailResult<String>>()
    open val asyncParallel = asyncParallelHook<suspend (String) -> Int>()
    open val asyncSeries = asyncSeriesHook<suspend (String) -> Int>()
    open val asyncSeriesBail = asyncSeriesBailHook<suspend (String) -> BailResult<String>>()
    open val asyncSeriesLoop = asyncSeriesLoopHook<suspend (String) -> LoopResult>()
    open val asyncSeriesWaterfall = asyncSeriesWaterfallHook<suspend (String) -> String>()
}
