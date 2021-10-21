package example

import com.intuit.hooks.AsyncSeriesHook
import com.intuit.hooks.HookContext
import com.intuit.hooks.SyncHook
import com.intuit.hooks.dsl.Hooks
import kotlinx.coroutines.runBlocking

val hook_types =
    """
// START hook_types
Basic, Waterfall, Bail, Loop
// END hook_types
"""

fun typed() {
    // START extendable_api
    class MyHook : SyncHook<((HookContext, Int) -> Unit)>() {
        fun call(value: Int) =
            super.call { f, context -> f(context, value) }

        fun tap(name: String, id: String, f: ((Int) -> Unit)) =
            super.tap(name, id) { _, newValue -> f(newValue) }
    }

    val myHook = MyHook()
    // END extendable_api

    // START typed
    myHook.tap("logger") { _, newValue: Int ->
        println("newValue: $newValue")
    }
    myHook.call(30)
    // END typed

    // START asynchronous
    class SimpleAsyncHook :
        AsyncSeriesHook<(suspend (HookContext) -> Unit)>() {

        suspend fun call() =
            super.call { f, context -> f(context) }
    }

    val simple = SimpleAsyncHook()
    runBlocking {
        simple.tap("some-network-plugin") {
            // do network call
        }

        simple.call()
    }
    // END asynchronous
}

// START concise_dsl
abstract class SomeHooks : Hooks() {
    open val syncHook = syncHook<() -> Unit>()
}
// END concise_dsl

val compiler_plugin =
    """
// START compiler_plugin
To make hooks easier to use
// END compiler_plugin
    """.trimIndent()

val gradle_plugin =
    """
// START gradle_plugin
plugins {
    id("com.intuit.hooks")
}
// END gradle_plugin
"""

val maven_plugin =
    """
// START maven_plugin
&lt;compilerPlugins&gt;
    &lt;plugin>hooks&lt;/plugin&gt;
&lt;/compilerPlugins&gt;
// END maven_plugin
"""
