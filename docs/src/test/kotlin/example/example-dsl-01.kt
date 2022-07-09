// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.intuit.hooks.example.exampleDsl01

import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks

internal abstract class GenericHooks : Hooks() {
    @Sync<(newSpeed: Int) -> Unit> abstract val sync: Hook
    @SyncBail<(Boolean) -> BailResult<Int>> abstract val syncBail: Hook
    @SyncLoop<(foo: Boolean) -> LoopResult> abstract val syncLoop: Hook
    @SyncWaterfall<(name: String) -> String> abstract val syncWaterfall: Hook
    @AsyncParallelBail<suspend (String) -> BailResult<String>> abstract val asyncParallelBail: Hook
    @AsyncParallel<suspend (String) -> Int> abstract val asyncParallel: Hook
    @AsyncSeries<suspend (String) -> Int> abstract val asyncSeries: Hook
    @AsyncSeriesBail<suspend (String) -> BailResult<String>> abstract val asyncSeriesBail: Hook
    @AsyncSeriesLoop<suspend (String) -> LoopResult> abstract val asyncSeriesLoop: Hook
    @AsyncSeriesWaterfall<suspend (String) -> String> abstract val asyncSeriesWaterfall: Hook
}

fun main() {
    val hooks = GenericHooksImpl()
    hooks.sync.tap("LoggerPlugin") { newSpeed: Int ->
        println("newSpeed: $newSpeed")
    }
    hooks.sync.call(30)
    // newSpeed: 30
}
