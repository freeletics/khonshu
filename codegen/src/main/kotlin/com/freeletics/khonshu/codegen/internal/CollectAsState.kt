package com.freeletics.khonshu.codegen.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.freeletics.khonshu.statemachine.StateMachine

@Composable
@InternalCodegenApi
public fun <S : Any> StateMachine<S, *>.asComposeState(): State<S?> {
    return produceState<S?>(initialValue = null) {
        state.collect { value = it }
    }
}
