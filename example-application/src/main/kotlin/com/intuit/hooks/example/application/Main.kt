package com.intuit.hooks.example.application

import com.intuit.hooks.example.library.car.Car
import com.intuit.hooks.example.library.car.Location
import com.intuit.hooks.example.library.car.Route
import kotlinx.coroutines.runBlocking

sealed class Navigation {
    abstract suspend fun calculateRoutes(source: Location, target: Location): List<Route>
}

object GoogleMapsService : Navigation() {
    override suspend fun calculateRoutes(source: Location, target: Location) = emptyList<Route>()
}

object BingMapsService : Navigation() {
    override suspend fun calculateRoutes(source: Location, target: Location) = emptyList<Route>()
}

object CachedRoutesService : Navigation() {
    private val cachedRoutes = hashMapOf<Pair<Location, Location>, List<Route>>()
    override suspend fun calculateRoutes(source: Location, target: Location) = cachedRoutes[source to target] ?: emptyList()

    fun cacheRoutes(source: Location, target: Location, routes: List<Route>) {
        cachedRoutes[source to target] = routes
    }
}

fun main() {
    val car = Car()

    car.hooks.brake.tap("WarningLampPlugin") { /** Should turn on warning lamps */ }
    car.hooks.accelerate.tap("LoggerPlugin") { newSpeed -> println("Accelerating to $newSpeed") }

    car.hooks.calculateRoutes.tap("GoogleMapsPlugin") { routesList, source, target ->
        routesList + GoogleMapsService.calculateRoutes(source, target)
    }

    car.hooks.calculateRoutes.tap("BingMapsPlugin") { routesList, source, target ->
        routesList + BingMapsService.calculateRoutes(source, target)
    }

    car.hooks.calculateRoutes.tap("CachedRoutesPlugin") { routesList, source, target ->
        routesList + CachedRoutesService.calculateRoutes(source, target)
    }

    val source = object : Location() {}
    val target = object : Location() {}

    val routes = runBlocking {
        car.useNavigationSystem(source, target)
    }
    println(routes)

    car.speed = 88
}
