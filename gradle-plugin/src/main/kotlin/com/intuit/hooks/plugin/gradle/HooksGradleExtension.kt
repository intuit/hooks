package com.intuit.hooks.plugin.gradle

/**
 * Any options to be used to configure the hooks-plugin.
 *
 * TODO: Still need to create a custom [CommandLineProcessor] to handle this
 */
public open class HooksGradleExtension {
    public var generatedSrcOutputDir: String? = null
}
