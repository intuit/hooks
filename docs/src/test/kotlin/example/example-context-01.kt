// This file was automatically generated from key-concepts.md by Knit tool. Do not edit.
package com.intuit.hooks.example.exampleContext01

import com.intuit.hooks.dsl.Hooks
import com.intuit.hooks.SyncHook

abstract class CarHooks : Hooks() {
    @Sync<(newSpeed: Int) -> Unit> abstract val accelerate: SyncHook<*>
}

class Car {
    val hooks = CarHooksImpl()

    var speed: Int = 0
        set(value) {
            hooks.accelerate.call(value)
        }
}

fun main() {
    val car = Car()

    car.hooks.accelerate.interceptTap { context, tapInfo ->
        println("${tapInfo.name} is doing it's job")
        context["hasMuffler"] = true
    }
    
    car.hooks.accelerate.tap("NoisePlugin") { context, newSpeed -> 
        println(if (context["hasMuffler"] == true) "Silence..." else "Vroom!")
    }
    
    car.speed = 20
    // NoisePlugin is doing it's job
    // Silence...
}
