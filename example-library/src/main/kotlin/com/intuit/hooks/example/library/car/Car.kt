package com.intuit.hooks.example.library.car

import com.intuit.hooks.dsl.Hooks

abstract class CarHooks : Hooks() {
    open val accelerate = syncHook<(newSpeed: Int) -> Unit>()
    open val brake = syncHook<() -> Unit>()
    open val calculateRoutes =
        asyncSeriesWaterfallHook<suspend (routesList: List<Route>, source: Location, target: Location) -> List<Route>>()
}

abstract class Location

class Route

class Car {

    val hooks = CarHooksImpl()

    var speed: Int = 0
        set(value) {
            field = value
            hooks.accelerate.call(value)
        }

    suspend fun useNavigationSystem(source: Location, target: Location): List<Route> {
        return hooks.calculateRoutes.call(emptyList(), source, target)
    }
}
