// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.intuit.hooks.example.exampleSynchook01

import com.intuit.hooks.HookContext
import com.intuit.hooks.SyncHook

class SimpleHook : SyncHook<(HookContext) -> Unit>() {
    fun call() = super.call { f, context -> f(context) }
}

fun main() {
    val hook = SimpleHook()
    hook.tap("logging") { context ->
        println("my hook was called")
    }
    hook.call()
}
