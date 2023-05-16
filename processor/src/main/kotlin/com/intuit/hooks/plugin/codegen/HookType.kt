package com.intuit.hooks.plugin.codegen

internal sealed class HookProperty {
    object Bail : HookProperty()
    object Loop : HookProperty()
    object Async : HookProperty()
    object Waterfall : HookProperty()
}

internal enum class HookType(vararg val properties: HookProperty) {
    SyncHook,
    SyncBailHook(HookProperty.Bail),
    SyncWaterfallHook(HookProperty.Waterfall),
    SyncLoopHook(HookProperty.Loop),
    AsyncParallelHook(HookProperty.Async),
    AsyncParallelBailHook(HookProperty.Async, HookProperty.Bail),
    AsyncSeriesHook(HookProperty.Async),
    AsyncSeriesBailHook(HookProperty.Async, HookProperty.Bail),
    AsyncSeriesWaterfallHook(HookProperty.Async, HookProperty.Waterfall),
    AsyncSeriesLoopHook(HookProperty.Async, HookProperty.Loop);

    companion object {
        val supportedHookTypes = values().map(HookType::name)

        val annotationDslMarkers = supportedHookTypes.map {
            it.dropLast(4)
        }
    }
}
