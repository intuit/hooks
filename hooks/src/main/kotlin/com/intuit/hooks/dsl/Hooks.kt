package com.intuit.hooks.dsl

import com.intuit.hooks.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val DEPRECATION_MESSAGE = "The migration to KSP requires DSL markers to be done outside of expression code."
private inline fun stub(): Nothing = throw NotImplementedError("Compiler stub called!")

public abstract class Hooks {
    // TODO: Make protected?
    protected annotation class Sync<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.Sync<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> syncHook(): SyncHook<*> = stub()

    protected annotation class SyncBail<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.SyncBail<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<BailResult<*>>> syncBailHook(): SyncBailHook<*, *, *> = stub()

    protected annotation class SyncWaterfall<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.SyncWaterfall<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> syncWaterfallHook(): SyncWaterfallHook<*, *> = stub()

    protected annotation class SyncLoop<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.SyncLoop<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<LoopResult>> syncLoopHook(): SyncLoopHook<*, *> = stub()

    protected annotation class AsyncParallel<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncParallel<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> asyncParallelHook(): AsyncParallelHook<*> = stub()

    protected annotation class AsyncParallelBail<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncParallelBail<F>"),
        DeprecationLevel.ERROR,
    )
    @ExperimentalCoroutinesApi protected fun <F : Function<BailResult<*>>> asyncParallelBailHook(): AsyncParallelBailHook<*, *> = stub()

    protected annotation class AsyncSeries<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeries<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> asyncSeriesHook(): AsyncSeriesHook<*> = stub()

    protected annotation class AsyncSeriesBail<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeriesBail<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<BailResult<*>>> asyncSeriesBailHook(): AsyncSeriesBailHook<*, *> = stub()

    protected annotation class AsyncSeriesWaterfall<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeriesWaterfall<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> asyncSeriesWaterfallHook(): AsyncSeriesWaterfallHook<*, *> = stub()

    protected annotation class AsyncSeriesLoop<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeriesLoop<F>"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<LoopResult>> asyncSeriesLoopHook(): AsyncSeriesLoopHook<*, *> = stub()
}

public typealias HooksDsl = Hooks
