//package com.intuit.hooks.plugin
//
//import arrow.meta.plugin.testing.CompilerTest
//import arrow.meta.plugin.testing.assertThis
//import org.junit.jupiter.api.Test
//
//class HookValidationErrors {
//    @Test
//    fun testNonInitializedHook() {
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    """
//        |package com.intuit.hooks.plugin.test
//        |import com.intuit.hooks.dsl.Hooks
//        |import com.intuit.hooks.SyncHook
//        |
//        |abstract class TestHooks : Hooks() {
//        |    abstract var testAbstractHook: SyncHook<() -> Int>
//        |}
//        """.source
//                },
//                assert = {
//                    failsWith { it.contains("testAbstractHook property needs to be initialized") }
//                }
//            )
//        )
//    }
//
//    @Test
//    fun testNonHookSignature() {
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    """
//        |package com.intuit.hooks.plugin.test
//        |import com.intuit.hooks.dsl.Hooks
//        |import com.intuit.hooks.SyncHook
//        |
//        |abstract class TestHooks : Hooks() {
//        |    open val notAHook = false
//        |}
//        """.source
//                },
//                assert = {
//                    failsWith { it.contains("property needs to be initialized with a DSL method") }
//                }
//            )
//        )
//    }
//
//    @Test
//    fun testMissingCodeGen() {
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    """
//        |package com.intuit.hooks.plugin.test
//        |import com.intuit.hooks.dsl.Hooks
//        |import com.intuit.hooks.SyncHook
//        |
//        |class NotActuallyAHook<F: Function<*>> : SyncHook<F>()
//        |fun <F: Function<*>> Hooks.notActuallyAHook() : NotActuallyAHook<F> = TODO()
//        |
//        |abstract class TestHooks : Hooks() {
//        |    open val foo = notActuallyAHook<() -> Unit>()
//        |}
//        """.source
//                },
//                assert = {
//                    failsWith { it.contains("This hook plugin has no code generator for NotActuallyAHook") }
//                }
//            )
//        )
//    }
//
//    @Test
//    fun testNonSuspendAsync() {
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    """
//        |package com.intuit.hooks.plugin.test
//        |import com.intuit.hooks.dsl.Hooks
//        |
//        |abstract class TestHooks : Hooks() {
//        |   open val asyncSeries = asyncSeriesHook<(String) -> Int>()
//        |}
//        """.source
//                },
//                assert = {
//                    failsWith { it.contains("Async hooks must be defined with a suspend function signature") }
//                }
//            )
//        )
//    }
//
//    @Test
//    fun testWaterfallZeroArity() {
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    """
//        |package com.intuit.hooks.plugin.test
//        |import com.intuit.hooks.dsl.Hooks
//        |
//        |abstract class TestHooks : Hooks() {
//        |    open val syncWaterfall = syncWaterfallHook<() -> String>()
//        |}
//        """.source
//                },
//                assert = {
//                    failsWith { it.contains("Waterfall hooks must take at least one parameter") }
//                }
//            )
//        )
//    }
//
//    @Test
//    fun multipleCompilerErrors() {
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    """
//        |package com.intuit.hooks.plugin.test
//        |import com.intuit.hooks.dsl.Hooks
//        |
//        |abstract class TestHooks : Hooks() {
//        |    open val realBad = asyncSeriesWaterfallHook<() -> String>()
//        |}
//        """.source
//                },
//                assert = {
//                    allOf(
//                        failsWith { it.contains("Async hooks must be defined with a suspend function signature") },
//                        failsWith { it.contains("Waterfall hooks must take at least one parameter") },
//                        failsWith { it.contains("Waterfall hooks must specify the same types for the first parameter and the return type") }
//                    )
//                }
//            )
//        )
//    }
//
//    @Test
//    fun testWaterfallParameterReturnEquality() {
//        assertThis(
//            CompilerTest(
//                config = { hookDependencies() },
//                code = {
//                    """
//        |package com.intuit.hooks.plugin.test
//        |import com.intuit.hooks.dsl.Hooks
//        |
//        |abstract class TestHooks : Hooks() {
//        |    open val syncWaterfall = syncWaterfallHook<(String) -> Boolean>()
//        |}
//        """.source
//                },
//                assert = {
//                    failsWith { it.contains("Waterfall hooks must specify the same types for the first parameter and the return type") }
//                }
//            )
//        )
//    }
//}
