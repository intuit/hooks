package com.intuit.hooks.plugin.codegen

internal sealed class HookProperty {
    object Bail : HookProperty()
    object Loop : HookProperty()
    object Async : HookProperty()
    object Waterfall : HookProperty()
}

internal enum class HookType(vararg val properties: HookProperty) {
    SyncHook {
        override fun generateClass(info: HookInfo): String {
            // todo: potentially protected
            return """|${info.property.visibility} inner class ${info.className} : ${info.superType}<${info.typeParameter}>() {
                      |    public fun call(${info.paramsWithTypes}): Unit = super.call { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    ${info.tapMethod}
                      |}"""
        }
    },
    SyncBailHook(HookProperty.Bail) {
        override fun generateClass(info: HookInfo): String {
            // todo: Potentially protected
            return """|${info.property.visibility} inner class ${info.className} : ${info.superType}<${info.typeParameter}, ${info.hookSignature.returnTypeType}>() {
                      |    public fun call(${info.paramsWithTypes}): ${info.hookSignature.nullableReturnTypeType} = super.call { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    ${info.tapMethod}
                      |}"""
        }
    },
    SyncWaterfallHook(HookProperty.Waterfall) {
        override fun generateClass(info: HookInfo): String {
            val accumulatorName = info.params.first().withoutType
            return """|${info.property.visibility} inner class ${info.className} : ${info.superType}<${info.typeParameter}, ${info.params.first().type}>() {
                      |    public fun call(${info.paramsWithTypes}): ${info.hookSignature.returnType} = super.call($accumulatorName,
                      |        invokeTap = { f, $accumulatorName, context -> f(context, ${info.paramsWithoutTypes}) },
                      |        invokeInterceptor = { f, context -> f(context, ${info.paramsWithoutTypes})}
                      |    )
                      |    ${info.tapMethod}
                      |}"""
        }
    },

    SyncLoopHook(HookProperty.Loop) {
        override fun generateClass(info: HookInfo): String {
            return """|${info.property.visibility} inner class ${info.className}: ${info.superType}<${info.typeParameter}, ${info.interceptParameter}>() {
                      |    public fun call(${info.paramsWithTypes}): Unit = super.call(
                      |         invokeTap = { f, context -> f(context, ${info.paramsWithoutTypes}) },
                      |         invokeInterceptor = { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    )
                      |    ${info.tapMethod}
                      |}"""
        }
    },

    AsyncParallelHook(HookProperty.Async) {
        override fun generateClass(info: HookInfo): String {
            return """|${info.property.visibility} inner class ${info.className}: ${info.superType}<${info.typeParameter}>() {
                      |    public suspend fun call(${info.paramsWithTypes}): Unit = super.call { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    ${info.tapMethod}
                      |}"""
        }
    },

    AsyncParallelBailHook(HookProperty.Async, HookProperty.Bail) {
        override fun generateClass(info: HookInfo): String {
            return """|@kotlinx.coroutines.ExperimentalCoroutinesApi
                      |${info.property.visibility} inner class ${info.className}: ${info.superType}<${info.typeParameter}, ${info.hookSignature.returnTypeType}>() {
                      |    public suspend fun call(concurrency: Int, ${info.paramsWithTypes}): ${info.hookSignature.nullableReturnTypeType} = super.call(concurrency) { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    ${info.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesHook(HookProperty.Async) {
        override fun generateClass(info: HookInfo): String {
            return """|${info.property.visibility} inner class ${info.className}: ${info.superType}<${info.typeParameter}>() {
                      |    public suspend fun call(${info.paramsWithTypes}): Unit = super.call { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    ${info.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesBailHook(HookProperty.Async, HookProperty.Bail) {
        override fun generateClass(info: HookInfo): String {
            return """|${info.property.visibility} inner class ${info.className}: ${info.superType}<${info.typeParameter}, ${info.hookSignature.returnTypeType}>() {
                      |    public suspend fun call(${info.paramsWithTypes}): ${info.hookSignature.nullableReturnTypeType} = super.call { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    ${info.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesWaterfallHook(HookProperty.Async, HookProperty.Waterfall) {
        override fun generateClass(info: HookInfo): String {
            val accumulatorName = info.params.first().withoutType
            return """|${info.property.visibility} inner class ${info.className} : ${info.superType}<${info.typeParameter}, ${info.params.first().type}>() {
                      |    public suspend fun call(${info.paramsWithTypes}): ${info.hookSignature.returnType} = super.call($accumulatorName,
                      |        invokeTap = { f, $accumulatorName, context -> f(context, ${info.paramsWithoutTypes}) },
                      |        invokeInterceptor = { f, context -> f(context, ${info.paramsWithoutTypes})}
                      |    )
                      |    ${info.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesLoopHook(HookProperty.Async, HookProperty.Loop) {
        override fun generateClass(info: HookInfo): String {
            return """|${info.property.visibility} inner class ${info.className}: ${info.superType}<${info.typeParameter}, ${info.interceptParameter}>() {
                      |    public suspend fun call(${info.paramsWithTypes}): Unit = super.call(
                      |         invokeTap = { f, context -> f(context, ${info.paramsWithoutTypes}) },
                      |         invokeInterceptor = { f, context -> f(context, ${info.paramsWithoutTypes}) }
                      |    )
                      |    ${info.tapMethod}
                      |}"""
        }
    };

    abstract fun generateClass(info: HookInfo): String

    companion object {
        val annotationDslMarkers = values().map {
            it.name.dropLast(4)
        }
    }
}
