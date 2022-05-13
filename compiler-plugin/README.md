# Kotlin Compiler Plugin

Built on [Arrow Meta](https://meta.arrow-kt.io/), the compiler plugin enables consumers to create hooks using the type-driven DSL provided in the hooks library. For the most part, Kotlin compiler plugins are easier to use when wrapped with a build system plugin. We currently have Gradle and Maven wrappers, but if you'd like to use outside those build systems, the compiler plugin is published as `com.intuit.hooks:compiler-plugin:$version`. If you do find yourself using the compiler plugin directly, we would love to hear about your use case and would appreciate any contributions or to support other build systems.

### Compiler Plugin DSL

With the compiler plugin registered to your project, you can now create hooks by defining a `Hooks` subclass. This gives you access to a collection of methods to create hook implementations based on the type signature passed into the method.

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

The compiler plugin uses this class to create new hook implementations and instances. Currently, the compiler plugin generates a new class that subclasses `GenericHooks` and overrides the member properties, which is why they are `open`. This is important to note, as otherwise, the code will not compile because the member property is final. When using the hooks DSL, you must follow these constraints:

1. `Hooks` subclass _must_ be abstract
2. All member properties that use the hooks DSL methods _must_ be open
3. Any `async` hook must take a `suspend` typed method
4. Bail hooks must return a `BailResult`
5. Loop hooks must return a `LoopResult`

Some of these constraints will give you an error when the compiler plugin runs and others will result in a generic compiler error, like stated above.

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
