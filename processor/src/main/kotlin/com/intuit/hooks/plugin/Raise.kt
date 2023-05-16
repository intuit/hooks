package com.intuit.hooks.plugin

import arrow.core.Nel
import arrow.core.nel
import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.ensure
import arrow.core.raise.recover
import kotlin.experimental.ExperimentalTypeInference

// Collection of [Raise] helpers for accumulating errors from a single error context

/** Helper for accumulating errors from single-error validators */
@RaiseDSL
@OptIn(ExperimentalTypeInference::class)
internal fun <Error, A> Raise<Nel<Error>>.ensure(@BuilderInference block: Raise<Error>.() -> A): A =
    recover(block) { e: Error -> raise(e.nel()) }

/** Helper for accumulating errors from single-error validators */
@RaiseDSL
public inline fun <Error> Raise<Nel<Error>>.ensure(condition: Boolean, raise: () -> Error) {
    recover({ ensure(condition, raise) }) { e: Error -> raise(e.nel()) }
}

/** Raise a _logical failure_ of type [Error] */
@RaiseDSL
public inline fun <Error> Raise<Nel<Error>>.raise(r: Error): Nothing {
    raise(r.nel())
}