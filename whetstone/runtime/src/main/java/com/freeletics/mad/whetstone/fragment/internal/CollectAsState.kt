package com.freeletics.mad.whetstone.fragment.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.freeletics.mad.statemachine.StateMachine
import kotlinx.coroutines.flow.collect

@Composable
@InternalWhetstoneApi
public fun <S : Any> StateMachine<S, *>.asComposeState(): State<S?> {
    return produceState<S?>(initialValue = null) {
        state.collect { value = it }
    }
}
