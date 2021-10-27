package com.intuit.hooks.example.library

import com.intuit.hooks.BailResult.*
import com.intuit.hooks.HookContext
import com.intuit.hooks.LoopResult
import com.intuit.hooks.example.library.generic.GenericHooksImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GenericHookTests {

    @Test
    fun `sync hook`() {
        val h = GenericHooksImpl().sync
        h.interceptRegister { info ->
            val keepTap = if (info.name == "bad") null else info
            println("Register - ${info.name} - ${keepTap != null}")
            keepTap
        }

        h.interceptCall { context, _ -> context.putIfAbsent("intercept1", true) }
        h.interceptCall { context, _ -> context.putIfAbsent("intercept2", true) }

        h.interceptTap { context, info ->
            context.increment("count")
            println("Tap - ${info.name}")
        }

        h.tap("bad") { _, x -> println("Bad! $x") }
        h.tap("hi") { _, x -> println("Hi! $x") }
        h.tap("bye") { _, x -> println("Bye! $x") }
        h.tap("what was in the context?!") { context, _ -> println("count: $context") }

        h.call(88)
    }

    @Test
    fun `sync loop`() {
        val h = GenericHooksImpl().syncLoop
        val interceptor = mockk<(HookContext, Boolean) -> Unit>()
        every { interceptor.invoke(any(), any()) } returns Unit

        h.interceptLoop(interceptor)
        h.tap("increment foo") { context, _ ->
            val count = context.increment("foo")
            if (count == 10) LoopResult.Continue else LoopResult.Restart
        }

        h.call(false)

        verify(exactly = 10) { interceptor.invoke(any(), any()) }
    }

    @Test
    fun `sync bail`() {
        val h = GenericHooksImpl().syncBail
        h.tap("continue") { _, _ -> Continue() }
        h.tap("bail") { _, _ -> Bail(2) }
        h.tap("continue again") { _, _ -> Assertions.fail("Should never have gotten here!") }

        val result = h.call(true)
        Assertions.assertEquals(2, result)
    }

    @Test
    fun `sync waterfall`() {
        val h = GenericHooksImpl().syncWaterfall
        h.tap("continue") { _, x -> "$x David" }
        h.tap("continue again") { _, x -> "$x Jeremiah" }

        val result = h.call("Kian")
        Assertions.assertEquals("Kian David Jeremiah", result)
    }

    @Test
    fun `async parallel bail hook`() = runBlocking {
        val h = GenericHooksImpl().asyncParallelBail
        h.tap("should never complete") { _, _ ->
            delay(100000)
            println("didn't work")
            Bail("shouldn't resolve here")
        }
        h.tap("shouldn't be canceled") { _, _ ->
            delay(1)
            println("should print this")
            Continue()
        }
        h.tap("cancel others") { _, _ ->
            delay(10)
            println("canceling others")
            Bail("foo")
        }

        val result = h.call(10, "Kian")
        Assertions.assertEquals("foo", result)
    }

    @Test
    fun `async parallel`() = runBlocking {
        val h = GenericHooksImpl().asyncParallel
        h.tap("foo") { _ ->
            delay(1)
            0
        }
        h.tap("bar") { _ ->
            delay(1)
            0
        }

        h.call("Kian")
    }

    @Test
    fun `async series`() = runBlocking {
        val h = GenericHooksImpl().asyncSeries
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

    @Test
    fun `async series bail`() = runBlocking {
        val h = GenericHooksImpl().asyncSeriesBail
        h.tap("continue") { _, _ -> Continue() }
        h.tap("bail") { _, _ -> Bail("bail now") }
        h.tap("continue again") { _, _ -> Assertions.fail("Should never have gotten here!") }

        val result = h.call("Kian")
        Assertions.assertEquals("bail now", result)
    }
    @Test
    fun `async series loop`() = runBlocking {
        var incrementedA = 0
        var incrementedB = 0

        val h = GenericHooksImpl().asyncSeriesLoop
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

    @Test
    fun `async series waterfall`() = runBlocking {
        val h = GenericHooksImpl().asyncSeriesWaterfall
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

    private fun HookContext.increment(key: String) =
        this.compute(key) { _, v -> if (v == null) 1 else (v as Int) + 1 }
}
