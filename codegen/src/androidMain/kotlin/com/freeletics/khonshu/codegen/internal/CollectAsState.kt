package com.freeletics.khonshu.codegen.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.freeletics.khonshu.statemachine.StateMachine

@Composable
@InternalCodegenApi
public fun <S : Any> StateMachine<S, *>.asComposeState(): State<S?> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember(this) { state }
    return produceState<S?>(initialValue = null, lifecycleOwner, state) {
        state.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .collect { value = it }
    }
}
