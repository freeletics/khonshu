package com.freeletics.khonshu.sample.feature.root

import com.freeletics.khonshu.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object RootState

sealed interface RootAction {
    data object ScreenButtonClicked : RootAction
    data object DialogButtonClicked : RootAction
    data object BottomSheetButtonClicked : RootAction
    data object ReplaceAllButtonClicked : RootAction
}

class RootStateMachine @Inject constructor(
    private val navigator: RootNavigator,
) : StateMachine<RootState, RootAction> {
    private val _state = MutableStateFlow(RootState)
    override val state: Flow<RootState> = _state

    override suspend fun dispatch(action: RootAction) {
        when (action) {
            RootAction.ScreenButtonClicked -> navigator.navigateToScreen()
            RootAction.DialogButtonClicked -> navigator.navigateToDialog()
            RootAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
            RootAction.ReplaceAllButtonClicked -> navigator.replaceAllWithNewRoot()
        }
    }
}
