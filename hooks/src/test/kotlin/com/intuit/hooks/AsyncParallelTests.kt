package com.intuit.hooks

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsyncParallelTests {
    class AsyncParallelHook1<T1, R> : AsyncParallelHook<suspend (HookContext, T1) -> R>() {
        suspend fun call(p1: T1) = super.call { f, context -> f(context, p1) }
    }

    @Test
    fun `register interceptors`() = runBlocking {
        var count = 0
        val h = AsyncParallelHook1<String, Int>()
        h.tap("foo") { _, _ ->
            delay(1)
            count++
            0
        }
        h.tap("bar") { _, _ ->
            delay(2)
            count++
            0
        }

        h.call("Kian")
        Assertions.assertEquals(2, count)
    }
}
