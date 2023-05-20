package com.freeletics.mad.sample.feature.screen

import com.freeletics.mad.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

sealed interface ScreenState

object Init : ScreenState

sealed interface ScreenAction {
    object BottomSheetButtonClicked : ScreenAction
}

class ScreenStateMachine @Inject constructor(
    private val navigator: ScreenNavigator,
) : StateMachine<ScreenState, ScreenAction> {
    private val _state = MutableStateFlow(Init)
    override val state: Flow<ScreenState> = _state

    override suspend fun dispatch(action: ScreenAction) {
        when (action) {
            ScreenAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
        }
    }
}
