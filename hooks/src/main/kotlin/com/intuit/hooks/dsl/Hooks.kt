package com.intuit.hooks.dsl

import com.intuit.hooks.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

public abstract class Hooks {
    protected fun <F : Function<*>> syncHook(): SyncHook<*> = object : SyncHook<F>() {}
    protected fun <F : Function<BailResult<*>>> syncBailHook(): SyncBailHook<*, *> = object : SyncBailHook<() -> BailResult<Any?>, Any?>() {}
    protected fun <F : Function<*>> syncWaterfallHook(): SyncWaterfallHook<*, *> = object : SyncWaterfallHook<F, Any?>() {}
    protected fun <F : Function<LoopResult>> syncLoopHook(): SyncLoopHook<*, *> = object : SyncLoopHook<F, F>() {}
    protected fun <F : Function<*>> asyncParallelHook(): AsyncParallelHook<*> = object : AsyncParallelHook<F>() {}
    @ExperimentalCoroutinesApi
    protected fun <F : Function<BailResult<*>>> asyncParallelBailHook(): AsyncParallelBailHook<*, *> = object : AsyncParallelBailHook<() -> BailResult<Any?>, Any?>() {}
    protected fun <F : Function<*>> asyncSeriesHook(): AsyncSeriesHook<*> = object : AsyncSeriesHook<F>() {}
    protected fun <F : Function<BailResult<*>>> asyncSeriesBailHook(): AsyncSeriesBailHook<*, *> = object : AsyncSeriesBailHook<() -> BailResult<Any?>, Any?>() {}
    protected fun <F : Function<*>> asyncSeriesWaterfallHook(): AsyncSeriesWaterfallHook<*, *> = object : AsyncSeriesWaterfallHook<F, Any?>() {}
    protected fun <F : Function<LoopResult>> asyncSeriesLoopHook(): AsyncSeriesLoopHook<*, *> = object : AsyncSeriesLoopHook<F, F>() {}
}

public typealias HooksDsl = Hooks
