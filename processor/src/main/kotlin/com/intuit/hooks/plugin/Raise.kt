@file:OptIn(ExperimentalTypeInference::class)

package com.intuit.hooks.plugin

import arrow.core.*
import arrow.core.raise.*
import kotlin.experimental.ExperimentalTypeInference

// Collection of [Raise] helpers for accumulating errors from a single error context

/** Helper for accumulating errors from single-error validators */
@RaiseDSL
internal fun <Error, A> Raise<Nel<Error>>.ensure(@BuilderInference block: Raise<Error>.() -> A): A =
    recover(block) { e: Error -> raise(e.nel()) }

/** Helper for accumulating errors from single-error validators */
@RaiseDSL
public inline fun <Error> Raise<Nel<Error>>.ensure(condition: Boolean, raise: () -> Error) {
    recover({ ensure(condition, raise) }) { e: Error -> raise(e.nel()) }
}

/** Raise a _logical failure_ of type [Error] in a multi-[Error] accumulator */
@RaiseDSL
public inline fun <Error> Raise<Nel<Error>>.raise(r: Error): Nothing {
    raise(r.nel())
}

@RaiseDSL
public inline fun <Error, A> Raise<NonEmptyList<Error>>.raiseAll(
    iterable: Iterable<A>,
    @BuilderInference transform: Raise<NonEmptyList<Error>>.(A) -> Unit
): List<Unit> = mapOrAccumulate(iterable) { arg ->
    recover<NonEmptyList<Error>, Unit>({ transform(arg) }) { errors ->
        this@raiseAll.raise(errors)
    }
}

/** Explicitly accumulate errors that may have been raised while processing each element */
context(Raise<NonEmptyList<Error>>)
@RaiseDSL
public inline fun <Error, A> Iterable<A>.accumulate(
    @BuilderInference operation: Raise<Nel<Error>>.(A) -> Unit
) {
    flatMap {
        recover({
            operation(it); emptyList()
        }) { it }
    }.toNonEmptyListOrNull()?.let { raise(it) }
}

/** [mapOrAccumulate] variant that accumulates errors from a validator that may raise multiple errors */
context(Raise<NonEmptyList<Error>>)
@RaiseDSL
public inline fun <Error, A, B> Sequence<A>.mapOrAccumulate( // TODO: Consider renaming
    @BuilderInference operation: Raise<Nel<Error>>.(A) -> B
): List<B> = toList().mapOrAccumulate(operation)

/** [mapOrAccumulate] variant that accumulates errors from a validator that may raise multiple errors */
context(Raise<NonEmptyList<Error>>)
@RaiseDSL
public inline fun <Error, A, B> Iterable<A>.mapOrAccumulate( // TODO: Consider renaming
    @BuilderInference operation: Raise<Nel<Error>>.(A) -> B
): List<B> = recover({
    mapOrAccumulate(this@mapOrAccumulate) { operation(it) }
}) { errors -> raise(errors.flatMap { it }) }

/** [mapOrAccumulate] variant that accumulates errors from a validator that may raise multiple errors */
context(Raise<NonEmptyList<Error>>)
@RaiseDSL
public inline fun <Error, A, B> NonEmptyList<A>.mapOrAccumulate( // TODO: Consider renaming
    @BuilderInference operation: Raise<Nel<Error>>.(A) -> B
): NonEmptyList<B> = recover({
    mapOrAccumulate(this@mapOrAccumulate) { operation(it) }
}) { errors -> raise(errors.flatMap { it }) }

/** [mapOrAccumulate] variant that accumulates errors from a validator that may raise multiple errors */
context(Raise<NonEmptyList<Error>>)
@RaiseDSL
public inline fun <Error, A, B> NonEmptySet<A>.mapOrAccumulate( // TODO: Consider renaming
    @BuilderInference operation: Raise<Nel<Error>>.(A) -> B
): NonEmptySet<B> = recover({
    mapOrAccumulate(this@mapOrAccumulate) { operation(it) }
}) { errors -> raise(errors.flatMap { it }) }
