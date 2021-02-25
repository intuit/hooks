package com.intuit.hooks

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

fun HookContext.increment(key: String) =
    this.compute(key) { _, v -> if (v == null) 1 else (v as Int) + 1 }

class SyncLoopHookTests {
    class LoopHook1<T1> : SyncLoopHook<(HookContext, T1) -> LoopResult, (HookContext, T1) -> Unit>() {
        fun call(p1: T1) = super.call(
            invokeTap = { f, context -> f(context, p1) },
            invokeInterceptor = { f, context -> f(context, p1) }
        )
    }

    @Test
    fun `interceptLoop allows you to intercept on every loop`() {
        val h = LoopHook1<String>()
        val interceptor = mockk<(HookContext, String) -> Unit>()
        every { interceptor.invoke(any(), any()) } returns Unit

        h.interceptLoop(interceptor)
        h.tap("increment foo") { context, _ ->
            val count = context.increment("foo")
            if (count == 10) LoopResult.Continue else LoopResult.Restart
        }

        h.call("foo")

        verify(exactly = 10) { interceptor.invoke(any(), any()) }
    }

    @Test
    fun `loop taps bail early`() {
        var incrementedA = 0
        var incrementedB = 0

        val h = LoopHook1<String>()
        h.tap("increment foo") { context, _ ->
            incrementedA += 1
            context.increment("foo")
            LoopResult.fromNullable(null)
        }

        h.tap("bail if foo is 6") { context, _ ->
            if (context["foo"] == 6) LoopResult.Restart else LoopResult.Continue
        }

        h.tap("read foo") { context, _ ->
            incrementedB += 1
            if ((context["foo"] as Int) < 10) LoopResult.Restart else LoopResult.Continue
        }

        h.call("Kian")

        Assertions.assertEquals(1, incrementedA - incrementedB)
    }
}
