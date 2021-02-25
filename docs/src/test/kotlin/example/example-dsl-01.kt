// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.intuit.hooks.example.exampleDsl01

import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks

abstract class GenericHooks : Hooks() {
    open val sync = syncHook<(newSpeed: Int) -> Unit>()
    open val syncBail = syncBailHook<(Boolean) -> BailResult<Int>>()
    open val syncLoop = syncLoopHook<(foo: Boolean) -> LoopResult>()
    open val syncWaterfall = syncWaterfallHook<(name: String) -> String>()
    open val asyncParallelBail = asyncParallelBailHook<suspend (String) -> BailResult<String>>()
    open val asyncParallel = asyncParallelHook<suspend (String) -> Int>()
    open val asyncSeries = asyncSeriesHook<suspend (String) -> Int>()
    open val asyncSeriesBail = asyncSeriesBailHook<suspend (String) -> BailResult<String>>()
    open val asyncSeriesLoop = asyncSeriesLoopHook<suspend (String) -> LoopResult>()
    open val asyncSeriesWaterfall = asyncSeriesWaterfallHook<suspend (String) -> String>()
}

fun main() {
    val hooks = GenericHooksImpl()
    hooks.sync.tap("LoggerPlugin") { newSpeed: Int ->
        println("newSpeed: $newSpeed")
    }
    hooks.sync.call(30)
    // newSpeed: 30
}
