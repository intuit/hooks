package com.intuit.hooks

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SyncWaterfallHookTests {
    class Hook1<T1> : SyncWaterfallHook<(HookContext, T1) -> T1, T1>() {
        fun call(p1: T1) = super.call(
            p1,
            invokeTap = { f, acc, context -> f(context, acc) },
            invokeInterceptor = { f, context -> f(context, p1) }
        )
    }

    class Hook2<T1, T2> : SyncWaterfallHook<(HookContext, T1, T2) -> T1, T1>() {
        fun call(p1: T1, p2: T2) = super.call(
            p1,
            invokeTap = { f, acc, context -> f(context, acc, p2) },
            invokeInterceptor = { f, context -> f(context, p1, p2) }
        )
    }

    @Test
    fun `waterfall taps work`() {
        val h = Hook1<String>()
        h.tap("continue") { _, x -> "$x David" }
        h.tap("continue again") { _, x -> "$x Jeremiah" }

        val result = h.call("Kian")
        Assertions.assertEquals("Kian David Jeremiah", result)
    }

    @Test
    fun `waterfall taps work with arity 2`() {
        val h = Hook2<String, Int>()
        h.tap("continue") { _, x, y -> "$x David" }
        h.tap("continue again") { _, x, y -> "$x Jeremiah" }

        val result = h.call("Kian", 3)
        Assertions.assertEquals("Kian David Jeremiah", result)
    }
}
