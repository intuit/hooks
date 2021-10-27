package com.intuit.hooks

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SyncHookTests {

    class Hook0<R> : SyncHook<(HookContext) -> R>() {
        fun call() = super.call { f, context -> f(context) }
    }

    class Hook1<T1, R> : SyncHook<(HookContext, T1) -> R>() {
        fun call(p1: T1) = super.call { f, context -> f(context, p1) }
    }

    class Hook2<T1, T2, R> : SyncHook<(HookContext, T1, T2) -> R>() {
        fun call(p1: T1, p2: T2) = super.call { f, context -> f(context, p1, p2) }
    }

    @Test
    fun `register interceptors`() {
        val h = Hook1<String, Unit>()
        h.interceptRegister { info ->
            val keepTap = if (info.name == "bad") null else info
            println("Register - ${info.name} - ${keepTap != null}")
            keepTap
        }

        h.interceptCall { context, _ -> context.putIfAbsent("intercept1", true) }
        h.interceptCall { context, _ -> context.putIfAbsent("intercept2", true) }

        h.interceptTap { context, info ->
            context.increment("count")
            println("Tap - ${info.name}")
        }

        h.tap("bad") { _, x -> println("Bad! $x") }
        h.tap("hi") { _, x -> println("Hi! $x") }
        h.tap("bye") { _, x -> println("Bye! $x") }
        h.tap("what was in the context?!") { context, _ -> println("count: $context") }

        h.call("Kian")
    }

    @Test
    fun `register interceptors with arity 2`() {
        val h = Hook2<String, String, Unit>()
        h.interceptRegister { info ->
            val keepTap = if (info.name == "bad") null else info
            println("Register - ${info.name} - ${keepTap != null}")
            keepTap
        }

        h.interceptCall { context, _, _ -> context.putIfAbsent("intercept1", true) }
        h.interceptCall { context, _, _ -> context.putIfAbsent("intercept2", true) }

        h.interceptTap { context, info ->
            context.increment("count")
            println("Tap - ${info.name}")
        }

        h.tap("bad") { _, x, y -> println("Bad! $x $y") }
        h.tap("hi") { _, x, y -> println("Hi! $x $y") }
        h.tap("bye") { _, x, y -> println("Bye! $x $y") }
        h.tap("what was in the context?!") { context, _, _ -> println("count: $context") }

        h.call("Kian", "Jeremiah")
    }

    @Test
    fun `can untap`() {
        val output = mutableListOf<Int>()
        val hook = Hook0<Unit>()

        hook.tap("first") {
            output.add(1)
        }
        val tap2 = hook.tap("second") {
            output.add(2)
        }!!
        hook.tap("third") {
            output.add(3)
        }

        hook.call()
        hook.untap(tap2)
        hook.call()

        Assertions.assertEquals(listOf(1, 2, 3, 1, 3), output)
    }

    @Test
    fun `can override when specifying an id`() {
        val output = mutableListOf<Int>()
        val hook = Hook0<Unit>()

        hook.tap("first") {
            output.add(1)
        }
        val tap2 = hook.tap("second") {
            output.add(2)
        }!!
        hook.tap("third") {
            output.add(3)
        }

        hook.call()

        hook.tap("second", tap2) {
            output.add(4)
        }

        hook.call()

        Assertions.assertEquals(listOf(1, 2, 3, 1, 3, 4), output)
    }
}
