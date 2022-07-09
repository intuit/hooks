package com.intuit.hooks.example.library.car

import com.intuit.hooks.AsyncSeriesWaterfallHook
import com.intuit.hooks.Hook
import com.intuit.hooks.SyncHook
import com.intuit.hooks.dsl.HooksDsl

public abstract class Location

public class Route

public class Car {

    public abstract class Hooks : HooksDsl() {

        @Sync<(newSpeed: Int) -> Unit>
        public abstract val accelerate: Hook

        @Sync<() -> Unit>
        public abstract val brake: Hook

        @AsyncSeriesWaterfall<suspend (routesList: List<Route>, source: Location, target: Location) -> List<Route>>
        public abstract val calculateRoutes: Hook
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
