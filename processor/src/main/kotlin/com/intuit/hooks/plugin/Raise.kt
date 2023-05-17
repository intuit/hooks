@file:OptIn(ExperimentalTypeInference::class)

package com.intuit.hooks.plugin

import arrow.core.*
import arrow.core.raise.*
import kotlin.experimental.ExperimentalTypeInference

// Collection of [Raise] helpers for accumulating errors from a single error context

/** Raise a _logical failure_ of type [Error] in a multi-[Error] accumulator */
@RaiseDSL
public inline fun <Error> Raise<Nel<Error>>.raise(r: Error): Nothing = raise(r.nel())

/** Execute [block] in singular `Raise<Error>` context such that singular [Error]s are re-raised in [this] scope as a [NonEmptyList] */
@RaiseDSL
internal fun <Error, A> Raise<Nel<Error>>.ensure(@BuilderInference block: Raise<Error>.() -> A): A =
    recover(block, ::raise)

/** [mapOrAccumulate] variant that accumulates errors from a validator that may raise multiple errors */
context(Raise<Nel<Error>>)
@RaiseDSL
public inline fun <Error, A, B> Sequence<A>.mapOrAccumulate(
    @BuilderInference operation: Raise<Nel<Error>>.(A) -> B
): List<B> = toList().mapOrAccumulate(operation)

/** [mapOrAccumulate] variant that accumulates errors from a validator that may raise multiple errors */
context(Raise<Nel<Error>>)
@RaiseDSL
public inline fun <Error, A, B> Iterable<A>.mapOrAccumulate(
    @BuilderInference operation: Raise<Nel<Error>>.(A) -> B
): List<B> = mapOrAccumulate(this, NonEmptyList<Error>::plus) { operation(it) }
