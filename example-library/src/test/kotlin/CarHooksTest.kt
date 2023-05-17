package com.intuit.hooks.example.library

import com.intuit.hooks.example.library.car.Car
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CarHooksTest {

    @Test
    fun testCarAccelerateHooks() {
        val car = Car()

        var accelerateTo: Int? = null
        car.hooks.accelerate.tap("LoggerPlugin") { newSpeed -> accelerateTo = newSpeed }

        car.speed = 88
        assertEquals(88, accelerateTo)
    }
}
