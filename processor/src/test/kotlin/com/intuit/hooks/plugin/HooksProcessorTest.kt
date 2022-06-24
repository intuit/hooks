package com.intuit.hooks.plugin

import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test

class HooksProcessorTest {

    @Test fun `generates simple sync hook`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @Sync<(String) -> Unit>
                abstract val testSyncHook: SyncHook<*>
            }
            """
        )

        val assertions = SourceFile.kotlin(
            "Assertions.kt",
            """
            import org.junit.jupiter.api.Assertions.*

            fun testHook() {
                var tapCalled = false
                val hooks = TestHooksImpl()
                hooks.testSyncHook.tap("test") { _, x -> tapCalled = true }
                hooks.testSyncHook.call("hello")
                assertTrue(tapCalled)
            }
            """
        )

        val (compilation, result) = compile(testHooks, assertions)
        result.assertOk()
        compilation.assertKspGeneratedSources("TestHooksImpl.kt")
        result.runCompiledAssertions()
    }

    @Test fun `generates hook class with the same package`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            package com.intuit.hooks.test

            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @Sync<(String) -> Unit>
                abstract val testSyncHook: SyncHook<*>
            }
            """
        )

        val (compilation, result) = compile(testHooks)
        result.assertOk()
        compilation.assertKspGeneratedSources("com.intuit.hooks.test.TestHooksImpl.kt")
    }

    @Test fun `generates hook with nested generic type params`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @Sync<(Map<List<Int>, List<String>>) -> Unit>
                abstract val testSyncHook: SyncHook<*>
            }
        """
        )

        val assertions = SourceFile.kotlin(
            "Assertions.kt",
            """
            import org.junit.jupiter.api.Assertions.*

            fun testHook() {
                val item = mapOf(listOf(1, 2, 3) to listOf("one", "two", "three"))
                var tappedItem: Map<List<Int>, List<String>>? = null
                val hooks = TestHooksImpl()
                hooks.testSyncHook.tap("test") { _, x -> tappedItem = x }
                hooks.testSyncHook.call(item)
                assertEquals(item, tappedItem)
            }
            """
        )

        val (compilation, result) = compile(testHooks, assertions)
        result.assertOk()
        compilation.assertKspGeneratedSources("TestHooksImpl.kt")
        result.runCompiledAssertions()
    }

    @Test fun `generates AsyncSeriesWaterfallHook`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.AsyncSeriesWaterfallHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @AsyncSeriesWaterfall<suspend (String) -> String>
                abstract val testAsyncSeriesWaterfallHook: AsyncSeriesWaterfallHook<*, *>
            }
        """
        )

        val assertions = SourceFile.kotlin(
            "Assertions.kt",
            """
            import kotlinx.coroutines.runBlocking
            import org.junit.jupiter.api.Assertions.*

            fun testHook() {
                val initialValue = "hello"
                var tappedItem: String? = null
                val hooks = TestHooksImpl()
                hooks.testAsyncSeriesWaterfallHook.tap("test") { _, x -> 
                    tappedItem = x
                    x + " world!"
                }
                val result = runBlocking { hooks.testAsyncSeriesWaterfallHook.call(initialValue) }
                assertEquals(initialValue, tappedItem)
                assertEquals("hello world!", result)
            }
            """
        )

        val (compilation, result) = compile(testHooks, assertions)
        result.assertOk()
        compilation.assertKspGeneratedSources("TestHooksImpl.kt")
        result.runCompiledAssertions()
    }

    @Test fun `smoke test`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.*
            import com.intuit.hooks.dsl.Hooks
            import kotlinx.coroutines.ExperimentalCoroutinesApi
            
            internal abstract class TestHooks : Hooks() {
                @Sync<(newSpeed: Int) -> Unit> abstract val sync: SyncHook<*>
                @SyncBail<(Boolean) -> BailResult<Int>> abstract val syncBail: SyncBailHook<*, *>
                @SyncLoop<(foo: Boolean) -> LoopResult> abstract val syncLoop: SyncLoopHook<*, *>
                @SyncWaterfall<(name: String) -> String> abstract val syncWaterfall: SyncWaterfallHook<*, *>
                @ExperimentalCoroutinesApi
                @AsyncParallelBail<suspend (String) -> BailResult<String>> abstract val asyncParallelBail: AsyncParallelBailHook<*, *>
                @AsyncParallel<suspend (String) -> Int> abstract val asyncParallel: AsyncParallelHook<*>
                @AsyncSeries<suspend (String) -> Int> abstract val asyncSeries: AsyncSeriesHook<*>
                @AsyncSeriesBail<suspend (String) -> BailResult<String>> abstract val asyncSeriesBail: AsyncSeriesBailHook<*, *>
                @AsyncSeriesLoop<suspend (String) -> LoopResult> abstract val asyncSeriesLoop: AsyncSeriesLoopHook<*, *>
                @AsyncSeriesWaterfall<suspend (String) -> String> abstract val asyncSeriesWaterfall: AsyncSeriesWaterfallHook<*, *>
            }
            """
        )

        val (compilation, result) = compile(testHooks)
        result.assertOk()
        compilation.assertKspGeneratedSources("TestHooksImpl.kt")
    }

    @Test fun `generates generic hook class`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks<T, U> : Hooks() {
                @Sync<(T) -> U>
                abstract val testSyncHook: SyncHook<*>
            }
            """
        )

        val assertions = SourceFile.kotlin(
            "Assertions.kt",
            """
            import org.junit.jupiter.api.Assertions.*

            fun testHook() {
                val item = "hello"
                var tappedValue: String? = null
                val hooks = TestHooksImpl<String, Unit>()
                hooks.testSyncHook.tap("test") { _, x -> tappedValue = x }
                hooks.testSyncHook.call(item)
                assertEquals(item, tappedValue)
            }
            """
        )

        val (compilation, result) = compile(testHooks, assertions)
        result.assertOk()
        compilation.assertKspGeneratedSources("TestHooksImpl.kt")
        result.runCompiledAssertions()
    }

    @Test fun `generates nested hook class`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.HooksDsl
            
            class Controller {
                abstract class Hooks : HooksDsl() {
                    @Sync<(String) -> Unit>
                    abstract val testSyncHook: SyncHook<*>
                }

                val hooks = ControllerHooksImpl()
            }
            """
        )

        val assertions = SourceFile.kotlin(
            "Assertions.kt",
            """
            import org.junit.jupiter.api.Assertions.*

            fun testHook() {
                val item = "hello"
                var tappedValue: String? = null
                val controller = Controller()
                controller.hooks.testSyncHook.tap("test") { _, x -> tappedValue = x }
                controller.hooks.testSyncHook.call(item)
                assertEquals(item, tappedValue)
            }
            """
        )

        val (compilation, result) = compile(testHooks, assertions)
        result.assertOk()
        compilation.assertKspGeneratedSources("ControllerHooksImpl.kt")
        result.runCompiledAssertions()
    }
}
