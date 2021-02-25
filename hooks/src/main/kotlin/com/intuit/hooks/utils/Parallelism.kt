package com.intuit.hooks.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal object Parallelism {
    // TODO: should we just pull in Reactor here?

    fun <T, R> Flow<T>.parallelMap(
        parallelism: Int,
        transform: suspend (value: T) -> R
    ): Flow<R> {
        require(parallelism > 0) { "Expected concurrency level greater than 0, but had $parallelism" }

        return flow {
            coroutineScope {
                val inputChannel = produce {
                    collect { send(it) }
                    close()
                }

                val outputChannel = Channel<R>(capacity = parallelism)

                // Launch $concurrency workers that consume from
                // input channel (fan-out) and publish to output channel (fan-in)
                val workers = (1..parallelism).map {
                    launch {
                        for (item in inputChannel) {
                            outputChannel.send(transform(item))
                        }
                    }
                }

                // Wait for all workers to finish and close the output channel
                launch {
                    workers.forEach { it.join() }
                    outputChannel.close()
                }

                // consume from output channel and emit
                outputChannel.consumeEach { emit(it) }
            }
        }
    }
}
