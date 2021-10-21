package com.intuit.hooks.plugin

internal class HookParameter(private val name: String?, val type: String, private val position: Int) {
    val withType get() = "$withoutType: $type"
    val withoutType get() = name ?: "p$position"
}

internal data class HookCodeGen(
    private val hookType: HookType,
    private val propertyName: String,
    val params: List<HookParameter>,
    val hookSignature: HookSignature,
    private val zeroArity: Boolean
) {
    val tapMethod get() = if (!zeroArity) "fun tap(name: String, f: ($hookSignature)) = super.tap(name) { _: HookContext, $paramsWithTypes -> f($paramsWithoutTypes) }" else ""
    val paramsWithTypes get() = params.joinToString(", ") { it.withType }
    val paramsWithoutTypes get() = params.joinToString(", ") { it.withoutType }
    fun generateClass() = this.hookType.generateClass(this)
    fun generateProperty() = "override val $propertyName: $className = $className()"
    fun generateImports(): List<String> = emptyList()
    private val isAsync get() = this.hookType.properties.contains(HookProperty.Async)
    val superType get() = this.hookType.toString()

    val className get() = "${this.propertyName.capitalizeFirstLetter()}$superType"
    val typeParameter get() = "(${if (isAsync) "suspend " else ""}(HookContext, $paramsWithTypes) -> ${hookSignature.returnType})"
    val interceptParameter get() = "${if (isAsync) "suspend " else ""}(HookContext, $paramsWithTypes) -> Unit"
}

internal enum class HookType(vararg val properties: HookProperty) {
    SyncHook {
        override fun generateClass(codeGen: HookCodeGen): String {
            // todo: potentially protected
            return """|inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}>() {
                      |    fun call(${codeGen.paramsWithTypes}) = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },
    SyncBailHook(HookProperty.Bail) {
        override fun generateClass(codeGen: HookCodeGen): String {
            // todo: Potentially protected
            return """|inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.hookSignature.returnTypeType}>() {
                      |    fun call(${codeGen.paramsWithTypes}) = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },
    SyncWaterfallHook(HookProperty.Waterfall) {
        override fun generateClass(codeGen: HookCodeGen): String {
            val accumulatorName = codeGen.params.first().withoutType
            return """|inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.params.first().type}>() {
                      |    fun call(${codeGen.paramsWithTypes}) = super.call($accumulatorName,
                      |        invokeTap = { f, $accumulatorName, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |        invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes})}
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    SyncLoopHook(HookProperty.Loop) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.interceptParameter}>() {
                      |    fun call(${codeGen.paramsWithTypes}) = super.call(
                      |         invokeTap = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |         invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncParallelHook(HookProperty.Async) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}>() {
                      |    suspend fun call(${codeGen.paramsWithTypes}) = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncParallelBailHook(HookProperty.Async, HookProperty.Bail) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|@kotlinx.coroutines.ExperimentalCoroutinesApi
                      |inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.hookSignature.returnTypeType}>() {
                      |    suspend fun call(concurrency: Int,  ${codeGen.paramsWithTypes}) = super.call(concurrency) { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesHook(HookProperty.Async) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}>() {
                      |    suspend fun call(${codeGen.paramsWithTypes}) = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesBailHook(HookProperty.Async, HookProperty.Bail) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.hookSignature.returnTypeType}>() {
                      |    suspend fun call(${codeGen.paramsWithTypes}) = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesWaterfallHook(HookProperty.Async, HookProperty.Waterfall) {
        override fun generateClass(codeGen: HookCodeGen): String {
            val accumulatorName = codeGen.params.first().withoutType
            return """|inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.params.first().type}>() {
                      |    suspend fun call(${codeGen.paramsWithTypes}) = super.call($accumulatorName,
                      |        invokeTap = { f, $accumulatorName, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |        invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes})}
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesLoopHook(HookProperty.Async, HookProperty.Loop) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.interceptParameter}>() {
                      |    suspend fun call(${codeGen.paramsWithTypes}) = super.call(
                      |         invokeTap = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |         invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    };

    open fun generateClass(codeGen: HookCodeGen): String = TODO()
}
