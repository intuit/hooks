package com.intuit.hooks

import com.intuit.hooks.SyncHookTests.Hook1
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

// TODO: Given that we require some knowledge of the return value for tapping the
//       hook, we can only apply this strategy for hooks that expect `Unit`. We
//       could potentially have a special hook type for this, called `StateHook`.
//       This'd remove the need for an intermediate capture class. We would want
//       to have helpers for converting `SyncHook<T1, Unit>` -> `StateHook<T1>`.

class Capture<T>(hook: Hook1<T, Unit>) {
    private var ref: WeakReference<T>? = null

    init {
        hook.tap("capture") { _, incoming ->
            ref = incoming?.let(::WeakReference)
        }
    }
}

// this is _kinda_ a hook.. but it's really just a wrapper that can only be instantiated with a reference to another hook
class StateHook<T>(hook: SyncHook<(HookContext, T) -> Unit>): SyncHook<(HookContext, T) -> Unit>() {
    private var ref: WeakReference<T>? = null

    init {
        hook.tap("StateHook") { ctx, incoming ->
            ref?.clear()
            ref = incoming?.let(::WeakReference)
            call(ctx, incoming)
        }
    }

    fun clear() {
        ref?.clear()
    }

    fun call(p1: T) = super.call { f, context -> f(context, p1) }

    fun call(context: HookContext, p1: T) = super.call { f, _ -> f(context, p1) }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = ref?.get()
}

fun <T> SyncHook<(HookContext, T) -> Unit>.asStateHook() = StateHook(this)

fun <T, R> SyncHook<(HookContext, T) -> Unit>.map(block: (T) -> R): SyncHook<(HookContext, R) -> Unit> {
    val transformed = Hook1<R, Unit>().asStateHook() // TODO: necessary for propagating context
    tap("map") { context, incoming -> transformed.call(context, block(incoming)) }
    return transformed
}

// the inherent problem here, is we need R to be nullable if T is nullable. We've tried a few approaches, but since nullability isn't captured
// as part of the JVM type system, we get platform declaration clashes when narrowing scenarios to different overloads
inline fun <T : Any?, R> SyncHook<(HookContext, T?) -> Unit>.flatMapNullable(crossinline block: (T?) -> SyncHook<(HookContext, R) -> Unit>?): SyncHook<(HookContext, R?) -> Unit> {
    // TODO: I hate that we need to return a R? state hook here - it means downstream consumers are no longer guaranteed to have a non-nullable
    val transformed = Hook1<R?, Unit>().asStateHook() // TODO: necessary for propagating context
    // TODO: Do we need to unregister the tap with a new incoming value?
    tap("flatMap") { _, incoming -> block(incoming)?.tap("capture", transformed::call) ?: transformed.call(null) }
    return transformed
}

inline fun <T : Any, R> SyncHook<(HookContext, T) -> Unit>.flatMap(crossinline block: (T) -> SyncHook<(HookContext, R) -> Unit>?): SyncHook<(HookContext, R) -> Unit> {
    // TODO: I hate that we need to return a R? state hook here - it means downstream consumers are no longer guaranteed to have a non-nullable
    val transformed = Hook1<R, Unit>().asStateHook() // TODO: necessary for propagating context
    // TODO: Do we need to unregister the tap with a new incoming value?
    tap("flatMap") { _, incoming -> block(incoming)?.tap("capture", transformed::call) ?: transformed.clear() }
    return transformed
}

// TODO: I wish this could have the same overload signature as [asStateHook] below, but the return type erasure isn't allowing for it
fun <T, R> SyncHook<(HookContext, T) -> Unit>.mapAsStateHook(block: (T) -> R): StateHook<R> = map(block).asStateHook()
//    val captured = Hook1<R, Unit>().asStateHook()
//    tap("asStateHook") { _, t ->
//        t?.let(block)?.let(captured::call)
//    }
//    return captured

inline fun <T : Any, reified R> SyncHook<(HookContext, T) -> Unit>.flatMapAsStateHook(crossinline block: (T) -> SyncHook<(HookContext, R) -> Unit>?): StateHook<R> =
    flatMap(block).asStateHook()

inline fun <T : Any?, reified R> SyncHook<(HookContext, T?) -> Unit>.flatMapNullableAsStateHook(crossinline block: (T?) -> SyncHook<(HookContext, R) -> Unit>?): StateHook<R?> =
    flatMapNullable(block).asStateHook()
//    val captured = Hook1<R, Unit>().asStateHook()
//    tap("asStateHook") { _, t ->
//        t?.let(block)?.tap("capture", captured::call)
//    }
//    return captured

class ImperativeTests {

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
        val innerNameState by outer.containerHook.mapAsStateHook(Container::name)
        val nullableInnerState by outer.nullableContainerHook.asStateHook()
        // TODO: Can we make the last part of this accept a lambda reference, essentially, preserve `null` for empty case and treat blocks as operating on non-nulls?
        val nullableInnerNameState by outer.nullableContainerHook.mapAsStateHook { it?.name }

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
        val nestedInnerState by outer.containerHook.flatMapAsStateHook(Container::containerHook)
        val nestedInnerNameState by outer.containerHook.flatMapAsStateHook(Container::containerHook).mapAsStateHook(Container::name)
        val nestedNullableInnerState by outer.containerHook.flatMapAsStateHook(Container::nullableContainerHook)
        val nestedNullableInnerNameState by outer.containerHook.flatMapAsStateHook(Container::nullableContainerHook).mapAsStateHook { it?.name }

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
        val nestedNullableOuterInnerState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.containerHook }
        val nestedNullableOuterInnerNameState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.containerHook }.mapAsStateHook { it?.name }
        val nestedNullableOuterNullableInnerState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.nullableContainerHook }
        val nestedNullableOuterNullableInnerNameState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.nullableContainerHook }.mapAsStateHook { it?.name }

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
        val nestedNullableOuterNullableInnerState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.nullableContainerHook }
        val nestedNullableOuterNullableInnerNameState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.nullableContainerHook }.mapAsStateHook { it?.name }

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
        val nestedOuterNullableInnerState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.containerHook }
        val nestedOuterNullableInnerNameState by outer.nullableContainerHook.flatMapNullableAsStateHook { it?.containerHook }.mapAsStateHook { it?.name }

        outer.nullableContainerHook.call(inner)
        inner.containerHook.call(nested)

        assertEquals(nested, nestedOuterNullableInnerState)
        assertEquals("nested", nestedOuterNullableInnerNameState)

        outer.nullableContainerHook.call(null)

        assertNull(nestedOuterNullableInnerState)
        assertNull(nestedOuterNullableInnerNameState)
    }
}
