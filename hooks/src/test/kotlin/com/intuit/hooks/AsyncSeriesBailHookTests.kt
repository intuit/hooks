package com.intuit.hooks

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsyncSeriesBailHookTests {
    class Hook1<T1, R : Any?> : AsyncSeriesBailHook<suspend (HookContext, T1) -> BailResult<R>, R>() {
        suspend fun call(p1: T1, default: (suspend (HookContext, T1) -> R)? = null): R? = super.call(
            { f, context -> f(context, p1) },
            default?.let {
                { context -> default(context, p1) }
            }
        )

        suspend fun call(p1: T1, default: (suspend (T1) -> R)) = call(p1) { _, arg1 ->
            default.invoke(arg1)
        }
    }

    @Test
    fun `null bail taps work`() = runBlocking {
        val calledA = mockk<suspend (HookContext, String) -> BailResult<Any>>()
        val calledB = mockk<suspend (HookContext, String) -> BailResult<Any>>()

        coEvery { calledA.invoke(any(), any()) } returns BailResult.Continue()
        coEvery { calledB.invoke(any(), any()) } returns BailResult.Continue()

        val h = Hook1<String, Any>()
        h.tap("continue", calledA)
        h.tap("continue again", calledB)
        val result = h.call("Kian")

        Assertions.assertNull(result)
        coVerify(exactly = 1) {
            calledA.invoke(any(), any())
            calledB.invoke(any(), any())
        }
    }

    @Test
    fun `bail taps bail early`() = runBlocking {
        val h = Hook1<String, Any?>()
        h.tap("bail") { _, _ -> BailResult.Bail("bail now") }
        h.tap("continue again") { _, _ -> Assertions.fail("Should never have gotten here!") }

        val result = h.call("Kian")
        Assertions.assertEquals("bail now", result)
    }

    @Test
    fun `bail taps can bail without return value`() = runBlocking {
        val h = Hook1<String, Unit>()
        h.tap("continue") { _, _ -> BailResult.Continue() }
        h.tap("bail") { _, _ -> BailResult.Bail(Unit) }
        h.tap("continue again") { _, _ -> Assertions.fail("Should never have gotten here!") }

        Assertions.assertEquals(Unit, h.call("David"))
    }

    @Test
    fun `bail call with default handler invokes without taps bailing`() = runBlocking {
        val h = Hook1<String, String>()
        h.tap("continue") { _, _ -> BailResult.Continue() }
        h.tap("continue again") { _, _ -> BailResult.Continue() }

        val result = h.call("David") { _, str ->
            str
        }

        Assertions.assertEquals("David", result)
    }

    @Test
    fun `bail call with default handler does not invoke with bail`() = runBlocking {
        val h = Hook1<String, String>()
        h.tap("continue") { _, _ -> BailResult.Continue() }
        h.tap("bail") { _, _ -> BailResult.Bail("bailing") }
        h.tap("continue again") { _, _ -> Assertions.fail("Should never have gotten here!") }

        val result = h.call("David") { str -> str }

        Assertions.assertEquals("bailing", result)
    }
}
