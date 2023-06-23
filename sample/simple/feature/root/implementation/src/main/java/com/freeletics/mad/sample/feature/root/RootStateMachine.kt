package com.freeletics.mad.sample.feature.root

import com.freeletics.mad.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object RootState

sealed interface RootAction {
    object ScreenButtonClicked : RootAction
    object DialogButtonClicked : RootAction
    object BottomSheetButtonClicked : RootAction
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
        }
    }
}
