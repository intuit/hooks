package com.intuit.hooks

import java.util.*
import kotlin.collections.HashMap

public typealias HookContext = HashMap<String, Any>

public open class Interceptors<F : Function<*>> {
    public var register: List<(TapInfo<F>) -> TapInfo<F>?> = emptyList(); private set
    public var tap: List<(HookContext, TapInfo<F>) -> Unit> = emptyList(); private set
    public var call: List<F> = emptyList(); private set

    public fun addRegisterInterceptor(interceptor: (TapInfo<F>) -> TapInfo<F>?) {
        register = register + interceptor
    }

    public fun invokeRegisterInterceptors(info: TapInfo<F>?): TapInfo<F>? = register.fold(info) { acc, interceptor ->
        acc?.let(interceptor)
    }

    public fun addTapInterceptor(interceptor: (HookContext, TapInfo<F>) -> Unit) {
        tap = tap + interceptor
    }

    public fun invokeTapInterceptors(taps: List<TapInfo<F>>, context: HookContext): Unit = tap.forEach { interceptor ->
        taps.forEach { tap ->
            interceptor.invoke(context, tap)
        }
    }

    public fun addCallInterceptor(interceptor: F) {
        call = call + interceptor
    }
}

public class TapInfo<FWithContext : Function<*>> internal constructor(
    public val name: String,
    public val id: String,
    public val type: String,
    public val f: FWithContext,
    // val stage: Int, // todo: maybe this should be forEachIndexed?
    // before?: string | Array // todo: do we even really need this?
)

public abstract class AsyncBaseHook<F : Function<*>>(type: String) : BaseHook<F>(type) {
    protected suspend fun setup(invokeCallInterceptor: suspend (F, HookContext) -> Any?, runTapInterceptors: Boolean = true): HookContext {
        val context: HookContext = hashMapOf()
        interceptors.call.forEach { interceptor ->
            invokeCallInterceptor(interceptor, context)
        }

        if (runTapInterceptors) {
            interceptors.invokeTapInterceptors(taps, context)
        }
        return context
    }
}

public abstract class SyncBaseHook<F : Function<*>>(type: String) : BaseHook<F>(type) {
    protected fun setup(invokeWithContext: (F, HookContext) -> Any?, runTapInterceptors: Boolean = true): HookContext {
        val context: HookContext = hashMapOf()
        interceptors.call.forEach { interceptor ->
            invokeWithContext(interceptor, context)
        }

        if (runTapInterceptors) {
            interceptors.invokeTapInterceptors(taps, context)
        }
        return context
    }
}

public sealed class Hook

public abstract class BaseHook<F : Function<*>>(private val type: String) : Hook() {
    protected var taps: List<TapInfo<F>> = emptyList(); private set
    protected open val interceptors: Interceptors<F> = Interceptors()

    /**
     * Tap the hook with [f].
     *
     * @param name human-readable identifier to make debugging easier
     *
     * @return an auto generated identifier token that can be used to [untap], null if tap was
     *         rejected by any of the register interceptors.
     */
    public fun tap(name: String, f: F): String? = tap(name, generateRandomId(), f)

    /**
     * Tap the hook with [f].
     *
     * @param name human-readable identifier to make debugging easier
     * @param id identifier token to register the [f] callback with. If another tap exists with
     *           the same [id], it will be overridden, which essentially shortcuts an [untap] call.
     *
     * @return identifier token that can be used to [untap], null if tap was rejected by any of the
     *         register interceptors.
     */
    public fun tap(name: String, id: String, f: F): String? {
        untap(id)

        return TapInfo(name, id, type, f).let(interceptors::invokeRegisterInterceptors)?.also {
            taps = taps + it
        }?.id
    }

    /** Remove tapped callback associated with the [id] returned from [tap] */
    public fun untap(id: String) {
        taps = taps.filter {
            it.id != id
        }
    }

    public fun interceptTap(f: (context: HookContext, tapInfo: TapInfo<F>) -> Unit) {
        interceptors.addTapInterceptor(f)
    }

    public fun interceptCall(f: F) {
        interceptors.addCallInterceptor(f)
    }

    public fun interceptRegister(f: (TapInfo<F>) -> TapInfo<F>?) {
        interceptors.addRegisterInterceptor(f)
    }

    /** Method to generate a random identifier for managing [TapInfo]s */
    protected open fun generateRandomId(): String = UUID.randomUUID().toString()
}
