package com.intuit.hooks.plugin

import arrow.meta.plugin.testing.CompilerTest
import arrow.meta.plugin.testing.assertThis
import org.junit.jupiter.api.Test

class HookTest {
    @Test
    fun testSyncHookCalled() {
        val testHookClass =
            """
|package com.intuit.hooks.plugin.test
|import com.intuit.hooks.dsl.Hooks
|
|abstract class TestHooks : Hooks() {
|    open val testSyncHook = syncHook<(String) -> Unit>()
|}
"""
        val testHookCall =
            """
|fun testHook() : Boolean { 
|   var tapCalled = false
|   val hooks = TestHooksImpl()
|   hooks.testSyncHook.tap("test") { _, x -> tapCalled = true }
|   hooks.testSyncHook.call("hello")
|   return tapCalled
|}"""
        assertThis(
            CompilerTest(
                config = { hookDependencies() },
                code = {
                    (testHookClass + testHookCall).source
                },

                assert = {
                    "testHook()".source.evalsTo(true)
                }
            )
        )
    }

    @Test
    fun testAsyncSeriesWaterfallHookCalled() {
        val testHookClass =
            """
|package com.intuit.hooks.plugin.test
|import com.intuit.hooks.dsl.Hooks
|import kotlinx.coroutines.runBlocking
|
|abstract class TestHooks : Hooks() {
|    open val testAsyncSeriesWaterfallHook = asyncSeriesWaterfallHook<suspend (String) -> String>()
|}
"""
        val testHookCall =
            """
|fun testHook() : Boolean { 
|   var tapCalled = false
|   val hooks = TestHooksImpl()
|   hooks.testAsyncSeriesWaterfallHook.tap("test") { x -> tapCalled = true; "asdf" }
|   runBlocking {
|       hooks.testAsyncSeriesWaterfallHook.call("someVal")
|   }
|   return tapCalled
|}"""

        assertThis(
            CompilerTest(
                config = { hookDependencies() },
                code = {
                    (testHookClass + testHookCall).source
                },

                assert = {
                    "testHook()".source.evalsTo(true)
                }
            )
        )
    }

    @Test
    fun smokeTest() {
        val allHooks =
            """
            |package com.intuit.hooks.plugin.test
            |import com.intuit.hooks.BailResult
            |import com.intuit.hooks.LoopResult
            |import com.intuit.hooks.dsl.Hooks
            |import kotlinx.coroutines.ExperimentalCoroutinesApi
            |
            |abstract class GenericHooks : Hooks() {
            |    open val sync = syncHook<(newSpeed: Int) -> Unit>()
            |    open val syncBail = syncBailHook<(Boolean) -> BailResult<Int>>()
            |    open val syncLoop = syncLoopHook<(foo: Boolean) -> LoopResult>()
            |    open val syncWaterfall = syncWaterfallHook<(name: String) -> String>()
            |    @ExperimentalCoroutinesApi
            |    open val asyncParallelBail = asyncParallelBailHook<suspend (String) -> BailResult<String>>()
            |    open val asyncParallel = asyncParallelHook<suspend (String) -> Int>()
            |    open val asyncSeries = asyncSeriesHook<suspend (String) -> Int>()
            |    open val asyncSeriesBail = asyncSeriesBailHook<suspend (String) -> BailResult<String>>()
            |    open val asyncSeriesLoop = asyncSeriesLoopHook<suspend (String) -> LoopResult>()
            |    open val asyncSeriesWaterfall = asyncSeriesWaterfallHook<suspend (String) -> String>()
            |}
            """.trimIndent()

        assertThis(
            CompilerTest(
                config = { hookDependencies() },
                code = { (allHooks).source },
                assert = {
                    compiles
                }
            )
        )
    }

    @Test
    fun testHookWithTypeParameter() {
        val testHookClass =
            """
|package com.intuit.hooks.plugin.test
|import com.intuit.hooks.dsl.Hooks
|
|abstract class TestHooks<T> : Hooks() {
|    open val testSyncHook = syncHook<(T) -> Unit>()
|}
"""
        val testHookCall =
            """
|fun testHook() : Boolean { 
|   var tapCalled = false
|   val hooks = TestHooksImpl<String>()
|   hooks.testSyncHook.tap("test") { _, x -> tapCalled = true }
|   hooks.testSyncHook.call("hello")
|   return tapCalled
|}"""
        assertThis(
            CompilerTest(
                config = { hookDependencies() },
                code = {
                    (testHookClass + testHookCall).source
                },

                assert = {
                    "testHook()".source.evalsTo(true)
                }
            )
        )
    }

    @Test
    fun `test nested hook class`() {
        val controllerClass =
            """
|package com.intuit.hooks.plugin.test
|import com.intuit.hooks.dsl.Hooks
|
|class Controller {
|   abstract class TestHooks : Hooks() {
|       open val testSyncHook = syncHook<() -> Unit>()
|   }
|   
|   val hooks = TestHooksImpl()
|}
"""

        val testScript =
            """
|fun testHook() : Boolean { 
|   var tapCalled = false
|   val controller = Controller()
|   controller.hooks.testSyncHook.tap("test") { _ -> tapCalled = true }
|   controller.hooks.testSyncHook.call()
|   return tapCalled
|}"""

        assertThis(
            CompilerTest(
                config = { hookDependencies() },
                code = {
                    (controllerClass + testScript).source
                },

                assert = {
                    "testHook()".source.evalsTo(true)
                }
            )
        )
    }
}
