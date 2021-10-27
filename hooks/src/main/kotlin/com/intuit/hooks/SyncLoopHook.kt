package com.intuit.hooks

public enum class LoopResult {
    Restart, Continue;

    public companion object {
        public fun fromNullable(x: Any?): LoopResult = if (x == null) Continue else Restart
    }
}

public class LoopInterceptors<F : Function<*>, FInterceptor : Function<*>> : Interceptors<F>() {
    public var loop: List<FInterceptor> = emptyList(); private set

    public fun addLoopInterceptor(f: FInterceptor) {
        loop = loop + f
    }
}

public abstract class SyncLoopHook<F : Function<LoopResult>, FInterceptor : Function<*>> : SyncBaseHook<F>("SyncLoopHook") {
    override val interceptors: LoopInterceptors<F, FInterceptor> = LoopInterceptors()

    protected fun call(
        invokeTap: (F, HookContext) -> LoopResult,
        invokeInterceptor: (FInterceptor, HookContext) -> Unit
    ) {
        val context = setup(invokeTap, runTapInterceptors = false)

        do {
            interceptors.invokeTapInterceptors(taps, context)
            interceptors.loop.forEach { interceptor ->
                invokeInterceptor(interceptor, context)
            }

            val restartFound = taps.find { tapInfo ->
                val result = invokeTap(tapInfo.f, context)
                result == LoopResult.Restart
            }
        } while (restartFound != null)
    }

    public fun interceptLoop(f: FInterceptor) {
        interceptors.addLoopInterceptor(f)
    }
}
