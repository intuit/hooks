package com.intuit.hooks.plugin

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.MetaCliProcessor
import arrow.meta.phases.CompilerContext

public open class HooksMetaPlugin : Meta {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> = listOf(hooks)
}
public class HooksMetaCliProcessor : MetaCliProcessor("hooks")
