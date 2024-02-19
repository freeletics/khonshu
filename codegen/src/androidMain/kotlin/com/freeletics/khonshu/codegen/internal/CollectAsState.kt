package com.freeletics.khonshu.codegen.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleDestroyedException
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.withCreated
import com.freeletics.khonshu.statemachine.StateMachine
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

@Composable
@InternalCodegenApi
public fun <S : Any> StateMachine<S, *>.asComposeState(): State<S?> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember(this) { state }
    return produceState<S?>(initialValue = null, lifecycleOwner, state) {
        // start state collection immediately and stop it on any downwards lifecycle event (e.g. pause/stop)
        state.runUntilDownEvent(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .collect { value = it }

        // after the initial collection was cancelled start collecting again whenever resuming
        state.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .collect { value = it }
    }
}

internal fun <T> Flow<T>.runUntilDownEvent(
    lifecycle: Lifecycle,
    lifecycleState: Lifecycle.State,
): Flow<T> = channelFlow {
    val cancelWorkEvent = requireNotNull(Lifecycle.Event.downFrom(lifecycleState)) {
        "Unsupported $lifecycleState"
    }
    val observer = LifecycleEventObserver { _, event ->
        if (event >= cancelWorkEvent) {
            close()
        }
    }

    try {
        // wait for the lifecycle to be at least created, this will
        // also cancel the flow when the lifecycle is already DESTROYED
        lifecycle.withCreated {}
        lifecycle.addObserver(observer)
        collect { send(it) }
    } catch (e: LifecycleDestroyedException) {
        // close before re-throwing CancellationException so that flow completes
        close()
        throw e
    } catch (e: ClosedSendChannelException) {
        // channel is already closed so just throw CancellationException
        throw CancellationException(e)
    } finally {
        lifecycle.removeObserver(observer)
    }
}
