# Plugin Architecture

Hooks can be used adhoc, but the advantage become clearer when paired with a plugin architecture. Plugins are a really simple concept to understand. Essentially, some construct has some basic functionality that can be extended. Plugins are accepted by the construct to extend said functionality. Let's take a look at a simple example.

## Simple example

Cars come with many features that can vary heavily depending on the make, model, trim, etc. Imagine a *very* simple car that has two features, braking and accelerating, however this car can come with different hardware peripherals, so it is hard to contain this logic within the base Car construct. This could be represented with hooks:

<!--- INCLUDE
import com.intuit.hooks.dsl.Hooks
-->

```kotlin
abstract class CarHooks : Hooks() {
    open val brake = syncHook<() -> Unit>()
    open val accelerate = syncHook<(newSpeed: Int) -> Unit>()
}
```

For simplicity's sake, say the car API exposes a `speed` API to change the speed:
```kotlin
class Car {
    
    val hooks = CarHooksImpl()

    var speed: Int = 0
        set(value) {
            if (value < field) hooks.brake.call()

            field = value
            hooks.accelerate.call(value)
        }
    
}
```

This essentially encapsulates the _core_ logic within the `Car` class, but delegates to the hook tappers to provide the actual implementation for braking and accelerating, with respect to the actual hardware or anything else that needs to respond to braking or accelerating.

```kotlin
fun main() {
    val car = Car()
    car.hooks.brake.tap("logging-brake-hook") {
        println("Turning on brake lights")
    }

    car.hooks.accelerate.tap("logging-accelerate-hook") { newSpeed ->
        println("Accelerating to $newSpeed")
    }
    car.speed = 30
    // accelerating to 30
    car.speed = 22
    // turning on brake lights
    // accelerating to 22
}
```

<!--- KNIT example-car-01.kt --> 

In the snippet above, loggers were tapped to each hook from the `car` reference. However, this does not ensure a good pattern for separation of logic because all tapped logic is contained where the `Car` was instantiated. Instead, we should organize this logic into various plugins that can be registered to the `Car` when its created. First, we modify the `Car` class to accept and handle plugins during instantiation.

<!--- INCLUDE
import com.intuit.hooks.dsl.Hooks

abstract class CarHooks : Hooks() {
    open val brake = syncHook<() -> Unit>()
    open val accelerate = syncHook<(newSpeed: Int) -> Unit>()
}
-->

```kotlin
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
```

Now that we have an interface for a `Car.Plugin`, we can move the logger taps to its own class (`object` in this case because plugins *can* be idempotent):

```kotlin
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
```

Then, just instantiate the `Car` with whatever plugins are desired:

```kotlin
fun main() {
    val car = Car(CarLoggerPlugin)
    car.speed = 30
    // accelerating to 30
    car.speed = 22
    // turning on brake lights
    // accelerating to 22
}
```

<!--- KNIT example-car-02.kt --> 

> You can get the full code [here](https://github.com/intuit/hooks/tree/master/docs/src/test/kotlin/example/example-car-02.kt).
