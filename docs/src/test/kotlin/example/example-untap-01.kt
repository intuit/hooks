// This file was automatically generated from key-concepts.md by Knit tool. Do not edit.
package com.intuit.hooks.example.exampleUntap01

import com.intuit.hooks.*

class SimpleHook : SyncHook<(HookContext) -> Unit>() {
    fun call() = super.call { f, context -> f(context) }
}

fun main() {

val simpleHook = SimpleHook()
val tap1 = simpleHook.tap("tap1") {
    println("doing something")
}!!

// to remove previously tapped function
simpleHook.untap(tap1)
// or to override previously tapped function
simpleHook.tap("tap1", tap1) {
    println("doing something else")
}
}
