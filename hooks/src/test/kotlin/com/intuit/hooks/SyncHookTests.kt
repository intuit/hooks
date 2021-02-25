package com.intuit.hooks

import org.junit.jupiter.api.Test

class SyncHookTests {

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
}
