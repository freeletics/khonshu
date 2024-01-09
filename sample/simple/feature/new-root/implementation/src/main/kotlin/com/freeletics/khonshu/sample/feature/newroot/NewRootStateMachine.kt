package com.freeletics.khonshu.sample.feature.newroot

import com.freeletics.khonshu.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object NewRootState

sealed interface NewRootAction {
    data object ScreenButtonClicked : NewRootAction
    data object DialogButtonClicked : NewRootAction
    data object BottomSheetButtonClicked : NewRootAction
    data object NavigateToRootButtonClicked : NewRootAction
}

class NewRootStateMachine @Inject constructor(
    private val navigator: NewRootNavigator,
) : StateMachine<NewRootState, NewRootAction> {
    private val _state = MutableStateFlow(NewRootState)
    override val state: Flow<NewRootState> = _state

    override suspend fun dispatch(action: NewRootAction) {
        when (action) {
            NewRootAction.ScreenButtonClicked -> navigator.navigateToScreen()
            NewRootAction.DialogButtonClicked -> navigator.navigateToDialog()
            NewRootAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
            NewRootAction.NavigateToRootButtonClicked -> navigator.navigateToRoot()
        }
    }
}
