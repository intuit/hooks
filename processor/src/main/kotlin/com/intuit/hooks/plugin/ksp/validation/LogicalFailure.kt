package com.intuit.hooks.plugin.ksp.validation

import com.google.devtools.ksp.symbol.KSFile

/** Base construct to represent a reason to not execute happy-path logic */
internal sealed interface LogicalFailure

/** Logical failure that can be ignored, valid edge case */
internal sealed interface EdgeCase : LogicalFailure {
    class NoHooksDefined(val file: KSFile) : EdgeCase
}

/** Logical failure that should probably be reported, something bad happened */
internal sealed interface ErrorCase : LogicalFailure {
    val message: String
}