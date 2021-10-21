package com.intuit.hooks

import java.util.*
import kotlin.collections.HashMap

public typealias HookContext = HashMap<String, Any>

public open class Interceptors<F : Function<*>> {
    // TODO: I don't love that I've really only made [taps] immutable.. this screams inconsistency
    public val register: MutableList<(TapInfo<F>) -> TapInfo<F>?> = mutableListOf()
    public val tap: MutableList<(HookContext, TapInfo<F>) -> Unit> = mutableListOf()
    public val call: MutableList<F> = mutableListOf()

    public fun invokeRegisterInterceptors(info: TapInfo<F>?): TapInfo<F>? =
        register.fold(info) { acc, interceptor ->
            acc?.let(interceptor)
        }

    public fun invokeTapInterceptors(taps: List<TapInfo<F>>, context: HookContext): Unit =
        tap.forEach { interceptor ->
            taps.forEach { tap ->
                interceptor.invoke(context, tap)
            }
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

public abstract class BaseHook<F : Function<*>>(private val type: String) {
    // TODO: This should probably be a var; private set to avoid concurrent modification exceptions
    protected var taps: List<TapInfo<F>> = emptyList(); private set
    protected open val interceptors: Interceptors<F> = Interceptors()

    public fun tap(name: String, f: F): String = tap(name, randomId(), f)

    public fun tap(name: String, id: String, f: F): String {
        val filtered = taps.filter {
            it.id != id
        }

        taps = TapInfo(name, id, type, f).let(interceptors::invokeRegisterInterceptors)?.let {
            filtered + it
        } ?: filtered

        return id
    }

    public fun untap(id: String) {
        taps = taps.filter {
            it.id != id
        }
    }

    public fun interceptTap(f: (context: HookContext, tapInfo: TapInfo<F>) -> Unit) {
        interceptors.tap.add(f)
    }

    public fun interceptCall(f: F) {
        interceptors.call.add(f)
    }

    public fun interceptRegister(f: (TapInfo<F>) -> TapInfo<F>?) {
        interceptors.register.add(f)
    }
}

public fun randomId(): String = UUID.randomUUID().toString()
