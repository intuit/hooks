package com.intuit.hooks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class AsyncParallelTests {
    class AsyncParallelHook1<T1, R> : AsyncParallelHook<suspend (HookContext, T1) -> R>() {
        suspend fun call(scope: CoroutineScope, p1: T1) = super.call(scope) { f, context -> f(context, p1) }
    }

    @Test
    fun `register interceptors`() = runBlocking {
        val h = AsyncParallelHook1<String, Int>()
        h.tap("foo") { _, _ ->
            delay(1)
            0
        }
        h.tap("bar") { _, _ ->
            delay(1)
            0
        }

        h.call(this, "Kian")
    }
}
