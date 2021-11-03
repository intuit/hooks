package com.intuit.hooks.example.library.car

import com.intuit.hooks.AsyncSeriesWaterfallHook
import com.intuit.hooks.SyncHook
import com.intuit.hooks.dsl.HooksDsl


public abstract class Location

public class Route

public class Car {

    public abstract class Hooks : HooksDsl() {
        public open val accelerate: SyncHook<*> = syncHook<(newSpeed: Int) -> Unit>()
        public open val brake: SyncHook<*> = syncHook<() -> Unit>()
        public open val calculateRoutes: AsyncSeriesWaterfallHook<*, *> =
            asyncSeriesWaterfallHook<suspend (routesList: List<Route>, source: Location, target: Location) -> List<Route>>()
    }

    public val hooks: CarHooksImpl = CarHooksImpl()

    public var speed: Int = 0
        set(value) {
            field = value
            hooks.accelerate.call(value)
        }

    public suspend fun useNavigationSystem(source: Location, target: Location): List<Route> {
        return hooks.calculateRoutes.call(emptyList(), source, target)
    }
}
