# Hooks

> **Note**
> 
> These instructions are for using the base hooks library by itself. Under most circumstances, it is advised to use the hooks processor with the DSL to limit the code overhead. However, it is still possible to utilize the hooks library directly if necessary.

### Installation

Add dependency to your `build.gradle(.kts)`:

<!--- PREFIX 
/*
----- SUFFIX
*/
-->

```kotlin
implementation("com.intuit.hooks:hooks:$version")
```

<!--- KNIT example-hooksinstallation-01.kt --> 

### Creating a hook

Each type of hook is exposed as an abstract class that can be subclassed to create a hook. Generally, the only additional functionality required for a hook is a public, typed `call` and `tap` method. These methods will effectively serve as the public API for your hook. Additionally, each of the base classes require a type parameter that represents the function signature for the `tap` method.

For example, consider a basic synchronous hook that doesn't take any parameters. This essentially could represent a simple eventing pub-sub model.

<!--- TEST_NAME SimpleHookTest --> 

<!--- INCLUDE
import com.intuit.hooks.HookContext
import com.intuit.hooks.SyncHook
-->

```kotlin
class SimpleHook : SyncHook<(HookContext) -> Unit>() {
    fun call() = super.call { f, context -> f(context) }
}
```

> Note here that the type parameter for `SyncHook` requires `HookContext` as the first parameter even though this use case doesn't merit a parameter. This is the case for all hooks, regardless of the hooks' arity.

`SimpleHook` can then be used directly to `tap` and `call`:

```kotlin
fun main() {
    val hook = SimpleHook()
    hook.tap("logging") { context ->
        println("my hook was called")
    }
    hook.call()
}
```

<!--- KNIT example-synchook-01.kt --> 

> You can get the full code [here](https://github.com/intuit/hooks/tree/main/docs/src/test/kotlin/example/example-synchook-01.kt).

We should expect the `tapped` function to be executed once the hook is `called`, which would print the following:

```text
my hook was called
```

<!--- TEST -->
