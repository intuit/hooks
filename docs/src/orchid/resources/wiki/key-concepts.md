# Key Concepts

### Nomenclature

To those new to this project, it might help to go over some keywords:

* Hook - some functionality in a construct that can be extended
* Tap - action taken by plugin to extend functionality
* Call - action taken by hook owner to invoke plugins
* Plugin - something that taps a hook

> A **plugin** can **tap** into a **hook** to provide additional functionality

### Hooks

The hooks library exposes a collection of different types of hooks that support different behavior. Each type of hook has some support for asynchronous evaluation through Kotlin coroutines.

| Type | Behavior | Async Support |
| ---- | -------- | ------------- |
| **Basic** | Basic hooks simply calls every function it tapped in a row | `SERIES`, `PARALLEL` |
| **Waterfall** | Waterfall hooks also call each tapped function in a row, however, it supports propagating return value from each function to the next function | `SERIES`, `PARALLEL` |
| **Bail** | Bail hooks allow exiting early with a return value. When any of the tapped function bails, the bail hook will stop executing the remaining ones | `PARALLEL` |
| **Loop** | When a plugin in a loop hook returns a non-undefined value the hook will restart from the first plugin. It will loop until all plugins return undefined. | `PARALLEL` |

### Untapping

Hooks that are tapped return a unique ID that can be used to `untap` from a hook, effectively removing that `tap` from the hook. For convenience, this ID can be specified when tapping the hook to easily override if the callback needs to be updated.

<!--- INCLUDE
import com.intuit.hooks.*
-->

```kotlin
class SimpleHook : SyncHook<(HookContext) -> Unit>() {
    fun call() = super.call { f, context -> f(context) }
}
```

<!--- INCLUDE

fun main() {
-->

```kotlin
val simpleHook = SimpleHook()
val tap1 = simpleHook.tap("tap1") {
    println("doing something")
}!!

// to remove previously tapped function
simpleHook.untap(tap1)
// or to override previously tapped function
simpleHook.tap("tap1", tap1) {
    println("doing something else")
}
```

<!--- INCLUDE
}
-->

<!--- KNIT example-untap-01.kt --> 

> With the register interceptors described below, calling `tap` is not guaranteed to actually tap the hook if the interceptor rejects it. In this case, the ID returned from `tap` will be `null`.

### Interceptors

Every hook provides support to register interceptors for different events:

| API | Description |
| --- | ----------- |
| `interceptCall` | Call interceptors will trigger when hooks are triggered and have access to the hook parameters, including the [`HookContext`](#hook-context) |
| `interceptTap` | Tap interceptors will trigger for each tapped plugin when a the hook is called and have access to the corresponding [`TapInfo`](https://intuit.github.io/hooks/kotlindoc/hooks/com/intuit/hooks/tapinfo/) and the [`HookContext`](#hook-context) |
| `interceptRegister` | Register interceptors will trigger when a plugin taps into a hook and have the opportunity to modify or remove the corresponding [`TapInfo`](https://intuit.github.io/hooks/kotlindoc/hooks/com/intuit/hooks/tapinfo/) |
| `interceptLoop` | Loop interceptors share the same signature as call interceptors, but are only available for **Loop** hooks, and will be triggered each time the hook evaluation loops |

### Hook context

Every plugin and some interceptors have access to a `HookContext`, which can be used to read or write arbitrary values for subsequent plugins and interceptors.

<!--- TEST_NAME HookContextTest -->

<!--- INCLUDE
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
-->

```kotlin
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
```

<!--- INCLUDE
}
-->

> This snippet might make more sense with respect to the example laid out in [plugin architecture](../plugin-architecture).

<!--- KNIT example-context-01.kt --> 

<!--- TEST
NoisePlugin is doing it's job
Silence...
-->
