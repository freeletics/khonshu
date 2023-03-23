package com.freeletics.mad.example.feature.a

import com.freeletics.mad.statemachine.StateMachine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

sealed interface FeatureAState

object Init : FeatureAState

sealed interface FeatureAAction

class FeatureAStateMachine @Inject constructor() : StateMachine<FeatureAState, FeatureAAction> {
    override val state: Flow<FeatureAState> = flowOf(Init)

    override suspend fun dispatch(action: FeatureAAction) {}
}
