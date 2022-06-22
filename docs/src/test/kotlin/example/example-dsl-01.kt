// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.intuit.hooks.example.exampleDsl01

// build.gradle(.kts)
plugins {
    id("com.google.devtools.ksp") version KSP_VERSION // >= 1.0.5
}

dependencies {
    ksp("com.intuit.hooks", "processor", HOOKS_VERSION)
}
import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks

internal abstract class GenericHooks : Hooks() {
    @Sync<(newSpeed: Int) -> Unit> abstract val sync: SyncHook<*>
    @SyncBail<(Boolean) -> BailResult<Int>> abstract val syncBail: SyncBailHook<*, *>
    @SyncLoop<(foo: Boolean) -> LoopResult> abstract val syncLoop: SyncLoopHook<*, *>
    @SyncWaterfall<(name: String) -> String> abstract val syncWaterfall: SyncWaterfallHook<*, *>
    @AsyncParallelBail<suspend (String) -> BailResult<String>> abstract val asyncParallelBail: AsyncParallelBailHook<*, *>
    @AsyncParallel<suspend (String) -> Int> abstract val asyncParallel: AsyncParallelHook<*>
    @AsyncSeries<suspend (String) -> Int> abstract val asyncSeries: AsyncSeriesHook<*>
    @AsyncSeriesBail<suspend (String) -> BailResult<String>> abstract val asyncSeriesBail: AsyncSeriesBailHook<*, *>
    @AsyncSeriesLoop<suspend (String) -> LoopResult> abstract val asyncSeriesLoop: AsyncSeriesLoopHook<*, *>
    @AsyncSeriesWaterfall<suspend (String) -> String> abstract val asyncSeriesWaterfall: AsyncSeriesWaterfallHook<*, *>
}

fun main() {
    val hooks = GenericHooksImpl()
    hooks.sync.tap("LoggerPlugin") { newSpeed: Int ->
        println("newSpeed: $newSpeed")
    }
    hooks.sync.call(30)
    // newSpeed: 30
}
