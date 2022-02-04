package com.freeletics.mad.navigator

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A base class for anything that exposes a [Flow] of [results]. Results will only be delivered
 * to one collector at a time.
 */
public abstract class ResultOwner<O> {
    private val _results = Channel<O>(capacity = Channel.UNLIMITED)

    /**
     * Emits any result passed to [onResult]. Results will only be delivered
     * to one collector at a time.
     */
    public val results: Flow<O> = _results.receiveAsFlow()

    /**
     * Deliver a new [result] to [results]. This method should be called by a
     * `NavEventNavigationHandler`.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public fun onResult(result: O) {
        val channelResult = _results.trySendBlocking(result)
        check(channelResult.isSuccess)
    }
}
