package com.intuit.hooks

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class AsyncSeriesTests {
    class AsyncHook1<T1, R> : AsyncSeriesHook<suspend (HookContext, T1) -> R>() {
        suspend fun call(p1: T1) = super.call { f, context -> f(context, p1) }
    }

    @Test
    fun `register interceptors`() = runBlocking {
        val h = AsyncHook1<String, Int>()
        h.tap("foo") { _, _ ->
            delay(1)
            0
        }
        h.tap("foo") { _, _ ->
            delay(1)
            0
        }

        h.call("Kian")
    }
}
