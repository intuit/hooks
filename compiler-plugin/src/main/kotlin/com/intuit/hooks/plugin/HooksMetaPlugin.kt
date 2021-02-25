package com.intuit.hooks.plugin

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.phases.CompilerContext

public open class HooksMetaPlugin : Meta {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> = listOf(hooks)
}
