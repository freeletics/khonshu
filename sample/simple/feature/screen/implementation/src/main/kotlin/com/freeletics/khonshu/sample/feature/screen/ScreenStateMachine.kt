package com.freeletics.khonshu.sample.feature.screen

import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.freeletics.khonshu.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class ScreenState(val number: Int)

sealed interface ScreenAction {
    data object ScreenButtonClicked : ScreenAction
    data object DialogButtonClicked : ScreenAction
    data object BottomSheetButtonClicked : ScreenAction
    data object ReplaceAllButtonClicked : ScreenAction
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
            ScreenAction.ReplaceAllButtonClicked -> navigator.replaceAllWithNewRoot()
        }
    }
}
