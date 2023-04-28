package com.freeletics.mad.sample.feature.a

import com.freeletics.mad.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface FeatureAState

object Init : FeatureAState

sealed interface FeatureAAction {
    object ButtonClicked : FeatureAAction
}

class FeatureAStateMachine @Inject constructor(
    private val navigator: FeatureANavigator,
) : StateMachine<FeatureAState, FeatureAAction> {
    private val _state = MutableStateFlow(Init)
    override val state: Flow<FeatureAState> = _state.asStateFlow()

    override suspend fun dispatch(action: FeatureAAction) {
        when (action) {
            FeatureAAction.ButtonClicked -> navigator.navigateToBottomSheet()
        }
    }
}
