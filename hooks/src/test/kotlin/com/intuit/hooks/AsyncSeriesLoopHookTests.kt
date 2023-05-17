package com.intuit.hooks

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsyncSeriesLoopHookTests {
    class LoopHook1<T1> : AsyncSeriesLoopHook<suspend (HookContext, T1) -> LoopResult, suspend (HookContext, T1) -> Unit>() {
        suspend fun call(p1: T1) = super.call(
            invokeTap = { f, context -> f(context, p1) },
            invokeInterceptor = { f, context -> f(context, p1) },
        )
    }

    @Test
    fun `interceptLoop allows you to intercept on every loop`() = runBlocking {
        val h = LoopHook1<String>()
        val interceptor = mockk<suspend (HookContext, String) -> Unit>()
        coEvery { interceptor.invoke(any(), any()) } returns Unit

        h.interceptLoop(interceptor)
        h.tap("increment foo") { context, _ ->
            delay(1)
            val count = context.increment("foo")
            if (count == 10) LoopResult.Continue else LoopResult.Restart
        }

        h.call("foo")

        coVerify(exactly = 10) { interceptor.invoke(any(), any()) }
    }

    @Test
    fun `loop taps bail early`() = runBlocking {
        var incrementedA = 0
        var incrementedB = 0

        val h = LoopHook1<String>()
        h.tap("increment foo") { context, _ ->
            delay(1)
            incrementedA += 1
            context.increment("foo")
            LoopResult.fromNullable(null)
        }

        h.tap("bail if foo is 6") { context, _ ->
            delay(1)
            if (context["foo"] == 6) LoopResult.Restart else LoopResult.Continue
        }

        h.tap("read foo") { context, _ ->
            delay(1)
            incrementedB += 1
            if ((context["foo"] as Int) < 10) LoopResult.Restart else LoopResult.Continue
        }

        h.call("Kian")

        Assertions.assertEquals(1, incrementedA - incrementedB)
    }
}
