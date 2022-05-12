package com.intuit.hooks.plugin

import com.intuit.hooks.plugin.ksp.HooksProcessor
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.addPreviousResultToClasspath
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HooksProcessorTest {

    fun compile(
        vararg sources: SourceFile,
        block: KotlinCompilation.() -> Unit = {
            symbolProcessorProviders = listOf(HooksProcessor.Provider())
            inheritClassPath = true
        }
    ): KotlinCompilation.Result = KotlinCompilation().apply {
        this.sources = sources.toList()
        block()
    }.compile()

    @Test
    fun testSyncHookCalled() {
        val testHookClass = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @Sync<(String) -> Unit>()
                abstract val testSyncHook: SyncHook<*>
            }
            """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            symbolProcessorProviders = listOf(HooksProcessor.Provider())
            sources = listOf(testHookClass)
            inheritClassPath = true
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
//        assertTrue(result.generatedFiles.map { it.name }.contains("TestHooksImpl.kt"))

        val testHookCall = SourceFile.kotlin(
            "Assertions.kt",
            """
            import com.intuit.hooks.*

            internal class TestHooksImpl : TestHooks() {

                override val testSyncHook: TestSyncHookSyncHook = TestSyncHookSyncHook()

                public inner class TestSyncHookSyncHook : SyncHook<((HookContext, p0: String) -> Unit)>() {
                    public fun call(p0: String): Unit = super.call { f, context -> f(context, p0) }
                    public fun tap(name: String, f: ((String) -> Unit)): String? = tap(name, generateRandomId(), f)
                    public fun tap(name: String, id: String, f: ((String) -> Unit)): String? = super.tap(name, id) { _: HookContext, p0: String -> f(p0) }
                }
            }
            
            fun testHook(): Boolean {
               var tapCalled = false
               val hooks = TestHooksImpl()
               hooks.testSyncHook.tap("test") { _, x -> tapCalled = true }
               hooks.testSyncHook.call("hello")
               return tapCalled
            }
        """
        )

        val assertionResult = KotlinCompilation().apply {
            sources = listOf(testHookClass, testHookCall)
            inheritClassPath = true
            addPreviousResultToClasspath(result)
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, assertionResult.exitCode)
        val assertions = assertionResult.classLoader.loadClass("AssertionsKt")
        assertions.declaredMethods.forEach {
            it.isAccessible = true
            assertTrue(it.invoke(null) as Boolean)
        }
    }

    @Test fun `hook params are generic`() {
        val testHookClass = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.SyncHook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @Sync<(Map<List<Int>, List<String>>) -> Unit>()
                abstract val testSyncHook: SyncHook<*>
            }
        """
        )

        val processorResult = compile(testHookClass)
        assertEquals(KotlinCompilation.ExitCode.OK, processorResult.exitCode)

        TODO()
    }

//    @Test
//    fun testAsyncSeriesWaterfallHookCalled() {
//        val testHookClass =
//            """
// |package com.intuit.hooks.plugin.test
// |import com.intuit.hooks.dsl.Hooks
// |import kotlinx.coroutines.runBlocking
// |
// |abstract class TestHooks : Hooks() {
// |    open val testAsyncSeriesWaterfallHook = asyncSeriesWaterfallHook<suspend (String) -> String>()
// |}
// """
//        val testHookCall =
//            """
// |fun testHook() : Boolean {
// |   var tapCalled = false
// |   val hooks = TestHooksImpl()
// |   hooks.testAsyncSeriesWaterfallHook.tap("test") { x -> tapCalled = true; "asdf" }
// |   runBlocking {
// |       hooks.testAsyncSeriesWaterfallHook.call("someVal")
// |   }
// |   return tapCalled
// |}"""
//
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    (testHookClass + testHookCall).source
//                },
//
//                assert = {
//                    "testHook()".source.evalsTo(true)
//                }
//            )
//        )
//    }
//
//    @Test
//    fun smokeTest() {
//        val allHooks =
//            """
//            |package com.intuit.hooks.plugin.test
//            |import com.intuit.hooks.BailResult
//            |import com.intuit.hooks.LoopResult
//            |import com.intuit.hooks.dsl.Hooks
//            |import kotlinx.coroutines.ExperimentalCoroutinesApi
//            |
//            |abstract class GenericHooks : Hooks() {
//            |    open val sync = syncHook<(newSpeed: Int) -> Unit>()
//            |    open val syncBail = syncBailHook<(Boolean) -> BailResult<Int>>()
//            |    open val syncLoop = syncLoopHook<(foo: Boolean) -> LoopResult>()
//            |    open val syncWaterfall = syncWaterfallHook<(name: String) -> String>()
//            |    @ExperimentalCoroutinesApi
//            |    open val asyncParallelBail = asyncParallelBailHook<suspend (String) -> BailResult<String>>()
//            |    open val asyncParallel = asyncParallelHook<suspend (String) -> Int>()
//            |    open val asyncSeries = asyncSeriesHook<suspend (String) -> Int>()
//            |    open val asyncSeriesBail = asyncSeriesBailHook<suspend (String) -> BailResult<String>>()
//            |    open val asyncSeriesLoop = asyncSeriesLoopHook<suspend (String) -> LoopResult>()
//            |    open val asyncSeriesWaterfall = asyncSeriesWaterfallHook<suspend (String) -> String>()
//            |}
//            """.trimIndent()
//
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = { (allHooks).source },
//                assert = {
//                    compiles
//                }
//            )
//        )
//    }
//
//    @Test
//    fun testHookWithTypeParameter() {
//        val testHookClass =
//            """
// |package com.intuit.hooks.plugin.test
// |import com.intuit.hooks.dsl.Hooks
// |
// |abstract class TestHooks<T> : Hooks() {
// |    open val testSyncHook = syncHook<(T) -> Unit>()
// |}
// """
//        val testHookCall =
//            """
// |fun testHook() : Boolean {
// |   var tapCalled = false
// |   val hooks = TestHooksImpl<String>()
// |   hooks.testSyncHook.tap("test") { _, x -> tapCalled = true }
// |   hooks.testSyncHook.call("hello")
// |   return tapCalled
// |}"""
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    (testHookClass + testHookCall).source
//                },
//
//                assert = {
//                    "testHook()".source.evalsTo(true)
//                }
//            )
//        )
//    }
//
//    @Test
//    fun `test nested hook class`() {
//        val controllerClass =
//            """
// |package com.intuit.hooks.plugin.test
// |import com.intuit.hooks.dsl.Hooks
// |
// |class Controller {
// |   abstract class TestHooks : Hooks() {
// |       open val testSyncHook = syncHook<() -> Unit>()
// |   }
// |
// |   val hooks = ControllerTestHooksImpl()
// |}
// """
//
//        val testScript =
//            """
// |fun testHook() : Boolean {
// |   var tapCalled = false
// |   val controller = Controller()
// |   controller.hooks.testSyncHook.tap("test") { _ -> tapCalled = true }
// |   controller.hooks.testSyncHook.call()
// |   return tapCalled
// |}"""
//
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    (controllerClass + testScript).source
//                },
//
//                assert = {
//                    "testHook()".source.evalsTo(true)
//                }
//            )
//        )
//    }
}
