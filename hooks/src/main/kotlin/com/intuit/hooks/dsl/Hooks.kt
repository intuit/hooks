package com.intuit.hooks.dsl

import com.intuit.hooks.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val DEPRECATION_MESSAGE = "The migration to KSP requires DSL markers to be done outside of expression code."
private inline fun stub(): Nothing = throw NotImplementedError("Compiler stub called!")

public abstract class Hooks {
    public annotation class Sync<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.Sync<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> syncHook(): SyncHook<*> = stub()

    public annotation class SyncBail<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.SyncBail<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<BailResult<*>>> syncBailHook(): SyncBailHook<*, *> = stub()

    public annotation class SyncWaterfall<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.SyncWaterfall<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> syncWaterfallHook(): SyncWaterfallHook<*, *> = stub()

    public annotation class SyncLoop<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.SyncLoop<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<LoopResult>> syncLoopHook(): SyncLoopHook<*, *> = stub()

    public annotation class AsyncParallel<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncParallel<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> asyncParallelHook(): AsyncParallelHook<*> = stub()

    public annotation class AsyncParallelBail<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncParallelBail<F>()"),
        DeprecationLevel.ERROR,
    )
    @ExperimentalCoroutinesApi protected fun <F : Function<BailResult<*>>> asyncParallelBailHook(): AsyncParallelBailHook<*, *> = stub()

    public annotation class AsyncSeries<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeries<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> asyncSeriesHook(): AsyncSeriesHook<*> = stub()

    public annotation class AsyncSeriesBail<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeriesBail<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<BailResult<*>>> asyncSeriesBailHook(): AsyncSeriesBailHook<*, *> = stub()

    public annotation class AsyncSeriesWaterfall<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeriesWaterfall<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<*>> asyncSeriesWaterfallHook(): AsyncSeriesWaterfallHook<*, *> = stub()

    public annotation class AsyncSeriesLoop<F : Function<*>>

    @Deprecated(
        DEPRECATION_MESSAGE,
        ReplaceWith("@Hooks.AsyncSeriesLoop<F>()"),
        DeprecationLevel.ERROR,
    )
    protected fun <F : Function<LoopResult>> asyncSeriesLoopHook(): AsyncSeriesLoopHook<*, *> = stub()

}

public typealias HooksDsl = Hooks
