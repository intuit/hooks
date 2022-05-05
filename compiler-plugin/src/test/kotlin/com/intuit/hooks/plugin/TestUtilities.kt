package com.intuit.hooks.plugin

import arrow.meta.plugin.testing.CompilerTest
import arrow.meta.plugin.testing.Config
import arrow.meta.plugin.testing.Dependency

fun CompilerTest.Companion.hookDependencies(): List<Config> {
    val hooks = Dependency("hooks")
    val prelude = Dependency("arrow-meta-prelude")
    val coroutines = Dependency("kotlinx-coroutines-core")

    return addDependencies(hooks, prelude, coroutines) + addMetaPlugins(HooksMetaPlugin())
}
