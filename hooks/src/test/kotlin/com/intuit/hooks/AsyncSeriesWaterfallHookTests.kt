package com.intuit.hooks

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsyncSeriesWaterfallHookTests {
    class Hook1<T1, R : T1> : AsyncSeriesWaterfallHook<suspend (HookContext, T1) -> R, R>() {
        suspend fun call(p1: R) = super.call(
            p1,
            invokeTap = { f, r, context -> f(context, r) },
            invokeInterceptor = { f, context -> f(context, p1) }
        )
    }

    @Test
    fun `waterfall taps work`() = runBlocking {
        val h = Hook1<String, String>()
        h.tap("continue") { _, x ->
            delay(1)
            "$x David"
        }
        h.tap("continue again") { _, x ->
            delay(1)
            "$x Jeremiah"
        }

        val result = h.call("Kian")
        Assertions.assertEquals("Kian David Jeremiah", result)
    }
}
