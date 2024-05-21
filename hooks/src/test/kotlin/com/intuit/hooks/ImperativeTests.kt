package com.intuit.hooks

import com.intuit.hooks.SyncHookTests.Hook1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

// TODO: Given that we require some knowledge of the return value for tapping the
//       hook, we can only apply this strategy for hooks that expect `Unit` (or
//       the same return type). We could potentially have a special hook type for
//       this, called `StateHook`. This'd remove the need for an intermediate capture
//       class. We would want to have helpers for converting `SyncHook<T1, Unit>` -> `StateHook<T1>`.

// this is _kinda_ a hook.. but it's really just a wrapper that can only be instantiated with a reference to another hook
class StateHook<T>(hook: SyncHook<(HookContext, T) -> Unit>): SyncHook<(HookContext, T) -> Unit>() {
    private var ref: WeakReference<T>? = null

    public val value: T? get() = ref?.get()

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

    init {
        // configure updates from incoming values from parent hook
        hook.tap("StateHook") { ctx, incoming ->
            onValue(ctx, incoming)
            call(ctx, incoming)
        }

        // configure updates from mutations on this hook
        tap("StateHook", ::onValue)

        // enable replay cache for new tappers
        interceptRegister { tap ->
            value?.let { tap.f(hashMapOf(), it) }
            tap
        }
    }

    private fun onValue(context: HookContext, incoming: T) {
        ref?.clear()
        ref = incoming?.let(::WeakReference)
    }

    internal fun clear() {
        ref?.clear()
    }

    internal fun call(p1: T) = call { f, context -> f(context, p1) }
    // special call to propagate incoming context from wrapped hook - maybe uplevel?
    internal fun call(context: HookContext, p1: T) = call { f, _ -> f(context, p1) }
}

fun <T> SyncHook<(HookContext, T) -> Unit>.asStateHook(): StateHook<T> = if (this is StateHook<T>) this else StateHook(this)

fun <T> SyncHook<(HookContext, T) -> Unit>.filter(predicate: (T) -> Boolean): SyncHook<(HookContext, T) -> Unit> {
    val filtered = Hook1<T, Unit>().asStateHook()
    tap("filter") { context, incoming -> if (predicate(incoming)) filtered.call(context, incoming) }
    return filtered
}

fun <T, R> SyncHook<(HookContext, T) -> Unit>.map(block: (T) -> R): SyncHook<(HookContext, R) -> Unit> {
    val transformed = Hook1<R, Unit>().asStateHook()
    tap("map") { context, incoming -> transformed.call(context, block(incoming)) }
    return transformed
}

// the inherent problem here, is we need R to be nullable if T is nullable. We've tried a few approaches, but since nullability isn't captured
// as part of the JVM type system, we get platform declaration clashes when narrowing scenarios to different overloads
// we _could_ potentially solve this by introducing a sealed type to encapsulate the return type of state hooks -- this'd enable us to
// ensure the statehook respects the parent hook type, while enabling us to capture "empty" state w/o using null
fun <T : Any?, R> SyncHook<(HookContext, T?) -> Unit>.flatMapNullable(block: (T?) -> SyncHook<(HookContext, R) -> Unit>?): SyncHook<(HookContext, R?) -> Unit> {
    val transformed = Hook1<R?, Unit>().asStateHook()
    tap("flatMapNullable") { _, incoming -> block(incoming)?.tap("flatMapNullable", transformed::call) ?: transformed.call(null) }
    return transformed
}

fun <T : Any, R> SyncHook<(HookContext, T) -> Unit>.flatMap(block: (T) -> SyncHook<(HookContext, R) -> Unit>?): SyncHook<(HookContext, R) -> Unit> {
    val transformed = Hook1<R, Unit>().asStateHook()
    tap("flatMap") { _, incoming -> block(incoming)?.tap("flatMap", transformed::call) ?: transformed.clear() }
    return transformed
}

// potentially add linter rule for .map().flatten() to just use .flatMap()
fun <T : SyncHook<(HookContext, R) -> Unit>, R> SyncHook<(HookContext, T) -> Unit>.flatten(): SyncHook<(HookContext, R) -> Unit> =
    flatMap { it }

fun <T : SyncHook<(HookContext, R) -> Unit>, R> SyncHook<(HookContext, T?) -> Unit>.flattenNullable(): SyncHook<(HookContext, R?) -> Unit> =
    flatMapNullable { it }

class ImperativeTests {

    @Test fun `simple state hook`() {
        val nameHook = Hook1<String, Unit>().asStateHook()
        val name: String? by nameHook

        assertNull(name)

        nameHook.call("this is my name")

        assertEquals("this is my name", name)

        var replayed = false
        nameHook.tap("test") { _, name ->
            assertEquals("this is my name", name)
            replayed = true
        }

        assertTrue(replayed)
    }

    class Machine {
        val someControllerHook = Hook1<SomeController, Unit>()

        // setting up a state hook at this level would enable tapping
        val someControllerState = someControllerHook.asStateHook()
    }

    data class SomeController(val id: String) {
        val nestedControllerHook = Hook1<SomeController, Unit>()
    }

    @Test fun `map state hook`() {
        val machine = Machine()
        val controller = SomeController("id")

        val controllerId by machine.someControllerHook
            .map(SomeController::id)
            .asStateHook()

        machine.someControllerHook.call(controller)

        assertEquals("id", controllerId)
    }

    @Test fun `flatmap state hook`() {
        val machine = Machine()
        val controller = SomeController("outer")
        val nestedController = SomeController("nested")

        val nestedControllerId by machine.someControllerHook
            .flatMap(SomeController::nestedControllerHook)
            .map(SomeController::id)
            .asStateHook()

//        var id: String? = null
//        machine.someControllerHook.tap("") { _, someController ->
//            someController.nestedControllerHook.tap("") { _, nestedController ->
//                id = nestedController.id
//            }
//        }

        machine.someControllerHook.call(controller)
        controller.nestedControllerHook.call(nestedController)

        assertEquals("nested", nestedControllerId)
    }

    @Test fun `as state hook`() {
        val hook = Hook1<String, Unit>()

        val stateHook = hook.asStateHook()
        val state by stateHook
        var stateHookValue: String? = null
        stateHook.tap("happy path") { _, value ->
            stateHookValue = value
        }

        // edge case, but maybe we just avoid an API like this
        val nestedStateHook = stateHook.asStateHook()
        val nestedState by nestedStateHook
        var nestedStateHookValue: String? = null
        nestedStateHook.tap("happy path") { _, value ->
            nestedStateHookValue = value
        }

        assertNull(state)
        hook.call("hello")
        assertEquals("hello", state)
        assertEquals("hello", stateHookValue)
        assertEquals("hello", nestedState)
        assertEquals("hello", nestedStateHookValue)
    }

    data class Container(val name: String) {
        val containerHook = Hook1<Container, Unit>()
        val nullableContainerHook = Hook1<Container?, Unit>()

        val containerState = containerHook.asStateHook()
        val nullableContainerState = nullableContainerHook.asStateHook()
    }

    @Test fun `nested state hook`() {
        val outer = Container("outer")
        val inner = Container("inner")
        val nested = Container("nested")

        // statehooks won't capture values pushed before creation
        outer.containerHook.call(inner)
        outer.nullableContainerHook.call(inner)

        // 1 level deep
        val innerState by outer.containerHook.asStateHook()
        val innerNameState by outer.containerHook.map(Container::name).asStateHook()
        val nullableInnerState by outer.nullableContainerHook.asStateHook()
        // TODO: Can we make the last part of this accept a lambda reference, essentially, preserve `null` for empty case and treat blocks as operating on non-nulls?
        val nullableInnerNameState by outer.nullableContainerHook.map { it?.name }.asStateHook()

        assertNull(innerState)
        assertNull(innerNameState)
        assertNull(nullableInnerState)
        assertNull(nullableInnerNameState)

        outer.containerHook.call(inner)

        assertEquals(inner, innerState)
        assertEquals("inner", innerNameState)
        assertNull(nullableInnerState)
        assertNull(nullableInnerNameState)

        outer.nullableContainerHook.call(inner)

        assertEquals(inner, nullableInnerState)
        assertEquals("inner", nullableInnerNameState)

        outer.nullableContainerHook.call(null)

        assertNull(nullableInnerState)
        assertNull(nullableInnerNameState)

        // 2 levels deep, with non-nullable outer
        val nestedInnerState by outer.containerHook.flatMap(Container::containerHook).asStateHook()
        val nestedInnerNameState by outer.containerHook.flatMap(Container::containerHook).map(Container::name).asStateHook()
        val nestedNullableInnerState by outer.containerHook.flatMap(Container::nullableContainerHook).asStateHook()
        val nestedNullableInnerNameState by outer.containerHook.flatMap(Container::nullableContainerHook).map { it?.name }.asStateHook()

        assertNull(nestedInnerState)
        assertNull(nestedInnerNameState)
        assertNull(nestedNullableInnerState)
        assertNull(nestedNullableInnerNameState)

        outer.containerHook.call(inner)

        assertNull(nestedInnerState)
        assertNull(nestedInnerNameState)
        assertNull(nestedNullableInnerState)
        assertNull(nestedNullableInnerNameState)

        inner.containerHook.call(nested)

        assertEquals(nested, nestedInnerState)
        assertEquals("nested", nestedInnerNameState)
        assertNull(nestedNullableInnerState)
        assertNull(nestedNullableInnerNameState)

        inner.nullableContainerHook.call(nested)

        assertEquals(nested, nestedNullableInnerState)
        assertEquals("nested", nestedNullableInnerNameState)

        inner.nullableContainerHook.call(null)

        assertNull(nestedNullableInnerState)
        assertNull(nestedNullableInnerNameState)

        // 2 levels deep, with nullable outer
        val nestedNullableOuterInnerState by outer.nullableContainerHook.flatMapNullable { it?.containerHook }.asStateHook()
        val nestedNullableOuterInnerNameState by outer.nullableContainerHook.flatMapNullable { it?.containerHook }.map { it?.name }.asStateHook()
        val nestedNullableOuterNullableInnerState by outer.nullableContainerHook.flatMapNullable { it?.nullableContainerHook }.asStateHook()
        val nestedNullableOuterNullableInnerNameState by outer.nullableContainerHook.flatMapNullable { it?.nullableContainerHook }.map { it?.name }.asStateHook()

        assertNull(nestedNullableOuterInnerState)
        assertNull(nestedNullableOuterInnerNameState)
        assertNull(nestedNullableOuterNullableInnerState)
        assertNull(nestedNullableOuterNullableInnerNameState)

        outer.nullableContainerHook.call(inner)

        assertNull(nestedNullableOuterInnerState)
        assertNull(nestedNullableOuterInnerNameState)
        assertNull(nestedNullableOuterNullableInnerState)
        assertNull(nestedNullableOuterNullableInnerNameState)

        inner.containerHook.call(nested)

        assertEquals(nested, nestedNullableOuterInnerState)
        assertEquals("nested", nestedNullableOuterInnerNameState)
        assertNull(nestedNullableOuterNullableInnerState)
        assertNull(nestedNullableOuterNullableInnerNameState)

        inner.nullableContainerHook.call(nested)

        assertEquals(nested, nestedNullableOuterNullableInnerState)
        assertEquals("nested", nestedNullableOuterNullableInnerNameState)

        inner.nullableContainerHook.call(null)

        assertNull(nestedNullableOuterNullableInnerState)
        assertNull(nestedNullableOuterNullableInnerNameState)

        // reset nullable to ensure we test clearing state from the top-level
        inner.nullableContainerHook.call(nested)
        outer.nullableContainerHook.call(null)

        assertNull(nestedNullableOuterInnerState)
        assertNull(nestedNullableOuterInnerNameState)
        assertNull(nestedNullableOuterNullableInnerState)
        assertNull(nestedNullableOuterNullableInnerNameState)
    }

    @Test fun `doubly nested nullable hooks`() {
        val outer = Container("outer")
        val inner = Container("inner")
        val nested = Container("nested")

        // 2 levels deep, with nullable outer
        val nestedNullableOuterNullableInnerState by outer.nullableContainerHook.flatMapNullable { it?.nullableContainerHook }.asStateHook()
        val nestedNullableOuterNullableInnerNameState by outer.nullableContainerHook.flatMapNullable { it?.nullableContainerHook }.map { it?.name }.asStateHook()

        outer.nullableContainerHook.call(inner)
        inner.nullableContainerHook.call(nested)

        assertEquals(nested, nestedNullableOuterNullableInnerState)
        assertEquals("nested", nestedNullableOuterNullableInnerNameState)

        outer.nullableContainerHook.call(null)

        assertNull(nestedNullableOuterNullableInnerState)
        assertNull(nestedNullableOuterNullableInnerNameState)
    }

    @Test fun `doubly nested outer non-nullable hooks`() {
        val outer = Container("outer")
        val inner = Container("inner")
        val nested = Container("nested")

        // 2 levels deep, with nullable outer
        val nestedOuterNullableInnerState by outer.nullableContainerHook.flatMapNullable { it?.containerHook }.asStateHook()
        val nestedOuterNullableInnerNameState by outer.nullableContainerHook.flatMapNullable { it?.containerHook }.map { it?.name }.asStateHook()

        outer.nullableContainerHook.call(inner)
        inner.containerHook.call(nested)

        assertEquals(nested, nestedOuterNullableInnerState)
        assertEquals("nested", nestedOuterNullableInnerNameState)

        outer.nullableContainerHook.call(null)

        assertNull(nestedOuterNullableInnerState)
        assertNull(nestedOuterNullableInnerNameState)
    }
}
