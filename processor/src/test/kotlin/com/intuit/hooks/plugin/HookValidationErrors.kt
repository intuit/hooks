
package com.intuit.hooks.plugin

import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Test

class HookValidationErrors {

    @Test fun `abstract property type not supported`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.Hook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                abstract val nonHookProperty: Int
            }
            """
        )

        val (_, result) = compile(testHooks)
        result.assertOk()
        result.assertContainsMessages("Abstract property type (Int) not supported")
    }

    @Test fun `hook property does not have any hook annotation`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.Hook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                abstract val syncHook: Hook
            }
            """
        )

        val (_, result) = compile(testHooks)
        result.assertOk()
        result.assertContainsMessages("Hook property must be annotated with a DSL annotation")
    }

    @Test fun `hook property has too many hook annotations`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.BailResult
            import com.intuit.hooks.Hook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @Sync<() -> Unit>
                @SyncBail<() -> BailResult<Int>>
                abstract val syncHook: Hook
            }
            """
        )

        val (_, result) = compile(testHooks)
        result.assertOk()
        result.assertContainsMessages("This hook has more than a single hook DSL annotation: [@Sync, @SyncBail]")
    }

    @Test fun `async hooks must has suspend modifier`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.Hook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @AsyncSeries<() -> Unit>
                abstract val syncHook: Hook
            }
            """
        )

        val (_, result) = compile(testHooks)
        result.assertOk()
        result.assertContainsMessages("Async hooks must be defined with a suspend function signature")
    }

    @Test fun `waterfall hook must have at least one parameter`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.Hook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @SyncWaterfall<() -> String>
                abstract val syncHook: Hook
            }
            """
        )

        val (_, result) = compile(testHooks)
        result.assertOk()
        result.assertContainsMessages("Waterfall hooks must take at least one parameter")
    }

    @Test fun `waterfall hook must return the same type of the first parameter`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.Hook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @SyncWaterfall<(Int, Int) -> Unit>
                abstract val syncHook: Hook
            }
            """
        )

        val (_, result) = compile(testHooks)
        result.assertOk()
        result.assertContainsMessages("Waterfall hooks must specify the same types for the first parameter and the return type")
    }

    @Test fun `multiple validation errors report at the same time`() {
        val testHooks = SourceFile.kotlin(
            "TestHooks.kt",
            """
            import com.intuit.hooks.Hook
            import com.intuit.hooks.dsl.Hooks
            
            internal abstract class TestHooks : Hooks() {
                @AsyncSeriesWaterfall<() -> String>
                abstract val realBad: Hook
                abstract val state: Int
            }
            """
        )

        val (_, result) = compile(testHooks)
        result.assertOk()
        result.assertContainsMessages(
            "Async hooks must be defined with a suspend function signature",
            "Waterfall hooks must take at least one parameter",
            "Waterfall hooks must specify the same types for the first parameter and the return type",
            "Abstract property type (Int) not supported",
        )
    }
}
