// This file was automatically generated from plugin-architecture.md by Knit tool. Do not edit.
package com.intuit.hooks.example.exampleCar02

import com.intuit.hooks.dsl.Hooks
import com.intuit.hooks.Hook

abstract class CarHooks : Hooks() {
    @Sync<() -> Unit>
    abstract val brake: Hook
    
    @Sync<(newSpeed: Int) -> Unit>
    abstract val accelerate: Hook
}

class Car(vararg plugins: Plugin) {
    
    val hooks = CarHooksImpl()

    var speed: Int = 0
        set(value) {
            if (value < field) hooks.brake.call()

            field = value
            hooks.accelerate.call(value)
        }
    
    init {
        plugins.forEach { it.apply(this) }
    }
 
    interface Plugin {
        fun apply(car: Car)
    }   
}

object CarLoggerPlugin : Car.Plugin {
    override fun apply(car: Car) {
        car.hooks.brake.tap("logging-brake-hook") {
            println("Turning on brake lights")
        }

        car.hooks.accelerate.tap("logging-accelerate-hook") { newSpeed ->
            println("Accelerating to $newSpeed")
        }
    }
}

fun main() {
    val car = Car(CarLoggerPlugin)
    car.speed = 30
    // accelerating to 30
    car.speed = 22
    // turning on brake lights
    // accelerating to 22
}
