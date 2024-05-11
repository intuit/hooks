package com.intuit.hooks.dsl

import com.intuit.hooks.*

private const val DEPRECATION_MESSAGE = "The migration to KSP requires DSL markers to be done outside of expression code."
private inline fun stub(): Nothing = throw NotImplementedError("Compiler stub called!")

public interface Hooks {
    public annotation class Sync<F : Function<*>>
    public annotation class SyncBail<F : Function<*>>
    public annotation class SyncWaterfall<F : Function<*>>
    public annotation class SyncLoop<F : Function<*>>
    public annotation class AsyncParallel<F : Function<*>>
    public annotation class AsyncParallelBail<F : Function<*>>
    public annotation class AsyncSeries<F : Function<*>>
    public annotation class AsyncSeriesBail<F : Function<*>>
    public annotation class AsyncSeriesWaterfall<F : Function<*>>
    public annotation class AsyncSeriesLoop<F : Function<*>>
}

public typealias HooksDsl = Hooks
