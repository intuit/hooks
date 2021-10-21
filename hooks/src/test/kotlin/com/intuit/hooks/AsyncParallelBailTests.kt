package com.intuit.hooks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class AsyncParallelBailTests {
    class AsyncParallelBailHook1<T1, R> : AsyncParallelBailHook<suspend (HookContext, T1) -> BailResult<R>, R>() {
        suspend fun call(concurrency: Int, p1: T1) = super.call(concurrency) { f, context -> f(context, p1) }
    }

    @Test
    fun `bail cancels others`() = runBlocking {
        val h = AsyncParallelBailHook1<String, String>()
        h.tap("should never complete") { _, _ ->
            delay(100000)
            println("didn't work")
            BailResult.Bail("shouldn't resolve here")
        }
        h.tap("shouldn't be canceled") { _, _ ->
            delay(1)
            println("should print this")
            BailResult.Continue()
        }
        h.tap("cancel others") { _, _ ->
            delay(10)
            println("canceling others")
            BailResult.Bail("foo")
        }

        val result = h.call(10, "Kian")
        Assertions.assertEquals("foo", result)
    }
}
