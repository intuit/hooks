package com.intuit.hooks.plugin.codegen

import com.intuit.hooks.plugin.validation.HookProperty


internal data class HookSignature(
    val text: String,
    val isSuspend: Boolean,
    val returnType: String,
    /** For hooks that return a wrapped result, like [BailResult], this is the inner type */
    val returnTypeType: String?,
) {
    val nullableReturnTypeType = "${returnTypeType}${if (returnTypeType?.last() == '?') "" else "?"}"

    override fun toString() = text
}

internal class HookParameter(private val name: String?, val type: String, private val position: Int) {
    val withType get() = "$withoutType: $type"
    val withoutType get() = name ?: "p$position"
}

internal data class HookCodeGen(
    private val hookType: HookType,
    private val propertyName: String,
    val params: List<HookParameter>,
    val hookSignature: HookSignature,
    private val zeroArity: Boolean,
    val visibility: String,
) {
    val tapMethod get() = if (!zeroArity) """
        public fun tap(name: String, f: $hookSignature): String? = tap(name, generateRandomId(), f)
        public fun tap(name: String, id: String, f: $hookSignature): String? = super.tap(name, id) { _: HookContext, $paramsWithTypes -> f($paramsWithoutTypes) }
    """.trimIndent() else ""
    val paramsWithTypes get() = params.joinToString(", ") { it.withType }
    val paramsWithoutTypes get() = params.joinToString(", ") { it.withoutType }
    fun generateClass() = this.hookType.generateClass(this)
    fun generateProperty() = (if (hookType == HookType.AsyncParallelBailHook) "@kotlinx.coroutines.ExperimentalCoroutinesApi\n" else "") +
        "override val $propertyName: $className = $className()"
    fun generateImports(): List<String> = emptyList()
    private val isAsync get() = this.hookType.properties.contains(HookProperty.Async)
    val superType get() = this.hookType.toString()

    val className get() = "${this.propertyName.replaceFirstChar(Char::titlecase)}$superType"
    val typeParameter get() = "(${if (isAsync) "suspend " else ""}(HookContext, $paramsWithTypes) -> ${hookSignature.returnType})"
    val interceptParameter get() = "${if (isAsync) "suspend " else ""}(HookContext, $paramsWithTypes) -> Unit"
}

internal enum class HookType(vararg val properties: HookProperty) {
    SyncHook {
        override fun generateClass(codeGen: HookCodeGen): String {
            // todo: potentially protected
            return """|${codeGen.visibility} inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}>() {
                      |    public fun call(${codeGen.paramsWithTypes}): Unit = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },
    SyncBailHook(HookProperty.Bail) {
        override fun generateClass(codeGen: HookCodeGen): String {
            // todo: Potentially protected
            return """|${codeGen.visibility} inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.hookSignature.returnTypeType}>() {
                      |    public fun call(${codeGen.paramsWithTypes}): ${codeGen.hookSignature.nullableReturnTypeType} = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },
    SyncWaterfallHook(HookProperty.Waterfall) {
        override fun generateClass(codeGen: HookCodeGen): String {
            val accumulatorName = codeGen.params.first().withoutType
            return """|${codeGen.visibility} inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.params.first().type}>() {
                      |    public fun call(${codeGen.paramsWithTypes}): ${codeGen.hookSignature.returnType} = super.call($accumulatorName,
                      |        invokeTap = { f, $accumulatorName, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |        invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes})}
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    SyncLoopHook(HookProperty.Loop) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|${codeGen.visibility} inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.interceptParameter}>() {
                      |    public fun call(${codeGen.paramsWithTypes}): Unit = super.call(
                      |         invokeTap = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |         invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncParallelHook(HookProperty.Async) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|${codeGen.visibility} inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}>() {
                      |    public suspend fun call(${codeGen.paramsWithTypes}): Unit = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncParallelBailHook(HookProperty.Async, HookProperty.Bail) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|@kotlinx.coroutines.ExperimentalCoroutinesApi
                      |${codeGen.visibility} inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.hookSignature.returnTypeType}>() {
                      |    public suspend fun call(concurrency: Int, ${codeGen.paramsWithTypes}): ${codeGen.hookSignature.nullableReturnTypeType} = super.call(concurrency) { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesHook(HookProperty.Async) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|${codeGen.visibility} inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}>() {
                      |    public suspend fun call(${codeGen.paramsWithTypes}): Unit = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesBailHook(HookProperty.Async, HookProperty.Bail) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|${codeGen.visibility} inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.hookSignature.returnTypeType}>() {
                      |    public suspend fun call(${codeGen.paramsWithTypes}): ${codeGen.hookSignature.nullableReturnTypeType} = super.call { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesWaterfallHook(HookProperty.Async, HookProperty.Waterfall) {
        override fun generateClass(codeGen: HookCodeGen): String {
            val accumulatorName = codeGen.params.first().withoutType
            return """|${codeGen.visibility} inner class ${codeGen.className} : ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.params.first().type}>() {
                      |    public suspend fun call(${codeGen.paramsWithTypes}): ${codeGen.hookSignature.returnType} = super.call($accumulatorName,
                      |        invokeTap = { f, $accumulatorName, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |        invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes})}
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    },

    AsyncSeriesLoopHook(HookProperty.Async, HookProperty.Loop) {
        override fun generateClass(codeGen: HookCodeGen): String {
            return """|${codeGen.visibility} inner class ${codeGen.className}: ${codeGen.superType}<${codeGen.typeParameter}, ${codeGen.interceptParameter}>() {
                      |    public suspend fun call(${codeGen.paramsWithTypes}): Unit = super.call(
                      |         invokeTap = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) },
                      |         invokeInterceptor = { f, context -> f(context, ${codeGen.paramsWithoutTypes}) }
                      |    )
                      |    ${codeGen.tapMethod}
                      |}"""
        }
    };

    abstract fun generateClass(codeGen: HookCodeGen): String
}
