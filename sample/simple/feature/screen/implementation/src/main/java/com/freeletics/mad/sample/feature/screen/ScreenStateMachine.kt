package com.freeletics.mad.sample.feature.screen

import com.freeletics.mad.sample.feature.screen.nav.ScreenRoute
import com.freeletics.mad.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class ScreenState(val number: Int)

sealed interface ScreenAction {
    object ScreenButtonClicked : ScreenAction
    object DialogButtonClicked : ScreenAction
    object BottomSheetButtonClicked : ScreenAction
}

class ScreenStateMachine @Inject constructor(
    route: ScreenRoute,
    private val navigator: ScreenNavigator,
) : StateMachine<ScreenState, ScreenAction> {
    private val _state = MutableStateFlow(ScreenState(route.number))
    override val state: Flow<ScreenState> = _state

    override suspend fun dispatch(action: ScreenAction) {
        when (action) {
            ScreenAction.ScreenButtonClicked -> navigator.navigateToScreen()
            ScreenAction.DialogButtonClicked -> navigator.navigateToDialog()
            ScreenAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
        }
    }
}
