# Hooks Processor

Built on the [Kotlin Symbol Processing API](https://kotlinlang.org/docs/ksp-overview.html#0), the Hooks processor enables consumers to create hooks using the type-driven DSL provided in the hooks library. KSP based processors are generally easy to apply to Gradle projects using the provided [plugin](https://kotlinlang.org/docs/ksp-quickstart.html#use-your-own-processor-in-a-project). However, we also have Gradle and Maven wrappers to ease the burden of KSP configuration. If you'd like to use outside those build systems, the processor is published as `com.intuit.hooks:processor:$version`. If you do find yourself using the processor directly, we would love to hear about your use case and would appreciate any contributions to support other build systems.

### Manual Gradle KSP configuration

<!--- INCLUDE
/** Throwaway code for knit (would be really nice if I could just specify a start for knit or exclude for knit)
-->

```kotlin
// build.gradle(.kts)
plugins {
    id("com.google.devtools.ksp") version KSP_VERSION // >= 1.0.5
}

dependencies {
    ksp("com.intuit.hooks", "processor", HOOKS_VERSION)
}
```

<!--- INCLUDE
*/
-->

<!--- KNIT example-throwaway-01.kt -->

### Processor DSL

With the processor configured in your project, you can now create hooks by defining a `Hooks` subclass. This gives you access to a collection of methods to create hook implementations based on the type signature passed into the method.

<!--- TEST_NAME HooksDSLTest -->

<!--- INCLUDE
import com.intuit.hooks.*
import com.intuit.hooks.dsl.Hooks
-->

```kotlin
internal abstract class GenericHooks : Hooks() {
    @Sync<(newSpeed: Int) -> Unit> abstract val sync: SyncHook<*>
    @SyncBail<(Boolean) -> BailResult<Int>> abstract val syncBail: SyncBailHook<*, *>
    @SyncLoop<(foo: Boolean) -> LoopResult> abstract val syncLoop: SyncLoopHook<*, *>
    @SyncWaterfall<(name: String) -> String> abstract val syncWaterfall: SyncWaterfallHook<*, *>
    @AsyncParallelBail<suspend (String) -> BailResult<String>> abstract val asyncParallelBail: AsyncParallelBailHook<*, *>
    @AsyncParallel<suspend (String) -> Int> abstract val asyncParallel: AsyncParallelHook<*>
    @AsyncSeries<suspend (String) -> Int> abstract val asyncSeries: AsyncSeriesHook<*>
    @AsyncSeriesBail<suspend (String) -> BailResult<String>> abstract val asyncSeriesBail: AsyncSeriesBailHook<*, *>
    @AsyncSeriesLoop<suspend (String) -> LoopResult> abstract val asyncSeriesLoop: AsyncSeriesLoopHook<*, *>
    @AsyncSeriesWaterfall<suspend (String) -> String> abstract val asyncSeriesWaterfall: AsyncSeriesWaterfallHook<*, *>
}
```

The processor uses this class to create new hook implementations and instances. Currently, the processor generates a new class that subclasses `GenericHooks` and overrides the member properties, which is why they need to be `abstract`. This is important to note, as otherwise, the code will not compile because the member property is final. When using the hooks DSL, you must follow these constraints:

1. `Hooks` subclass _must_ be abstract
2. All member properties that use the hooks DSL methods _must_ be abstract
3. Hook property types can include star projection, but should be the same hook type
4. Any `async` hook must take a `suspend` typed method
5. Bail hooks must return a `BailResult`
6. Loop hooks must return a `LoopResult`

Most of these constraints should give you an error when the processor runs, however others might result in a generic compiler error, like stated above.

The generated class name will be `${name}Impl`, thus the snippet above could be used in the following manner:

```kotlin
fun main() {
    val hooks = GenericHooksImpl()
    hooks.sync.tap("LoggerPlugin") { newSpeed: Int ->
        println("newSpeed: $newSpeed")
    }
    hooks.sync.call(30)
    // newSpeed: 30
}
```

<!--- KNIT example-dsl-01.kt -->

<!--- TEST
newSpeed: 30
-->
