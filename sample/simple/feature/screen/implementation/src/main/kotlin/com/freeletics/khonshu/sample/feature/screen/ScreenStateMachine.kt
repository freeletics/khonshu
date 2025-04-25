package com.freeletics.khonshu.sample.feature.screen

import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.freeletics.khonshu.statemachine.StateMachine
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

data class ScreenState(val number: Int, val result: String? = null)

sealed interface ScreenAction {
    data object ScreenButtonClicked : ScreenAction

    data object DialogButtonClicked : ScreenAction

    data object BottomSheetButtonClicked : ScreenAction

    data object ReplaceAllButtonClicked : ScreenAction

    data object ScreenForResultButtonClicked : ScreenAction
}

class ScreenStateMachine @Inject constructor(
    route: ScreenRoute,
    private val navigator: ScreenNavigator,
) : StateMachine<ScreenState, ScreenAction> {
    private val _state = MutableStateFlow(ScreenState(route.number))
    override val state: Flow<ScreenState> = merge(_state, observeScreenResults())

    override suspend fun dispatch(action: ScreenAction) {
        when (action) {
            ScreenAction.ScreenButtonClicked -> navigator.navigateToScreen()
            ScreenAction.DialogButtonClicked -> navigator.navigateToDialog()
            ScreenAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
            ScreenAction.ReplaceAllButtonClicked -> navigator.replaceAllWithNewRoot()
            ScreenAction.ScreenForResultButtonClicked -> navigator.navigateToScreenForResult()
        }
    }

    private fun observeScreenResults() = navigator.destinationResult.results
        .map {
            val currentState = _state.value
            currentState.copy(result = it.data)
        }
}
