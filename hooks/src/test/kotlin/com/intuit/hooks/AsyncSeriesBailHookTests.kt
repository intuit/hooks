package com.intuit.hooks

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsyncSeriesBailHookTests {
    class Hook1<T1, R : Any?> : AsyncSeriesBailHook<suspend (HookContext, T1) -> BailResult<R>, R>() {
        suspend fun call(p1: T1): R? = super.call { f, context -> f(context, p1) }
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
    fun `bail taps can bail without return value`() {
        val h = SyncBailHookTests.Hook1<String, Unit>()
        h.tap("continue") { _, _ -> BailResult.Continue() }
        h.tap("bail") { _, _ -> BailResult.Bail(Unit) }
        h.tap("continue again") { _, _ -> Assertions.fail("Should never have gotten here!") }

        Assertions.assertEquals(Unit, h.call("David"))
    }
}
