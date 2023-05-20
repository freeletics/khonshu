package com.freeletics.mad.sample.feature.root

import com.freeletics.mad.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

sealed interface RootState

object Init : RootState

sealed interface RootAction {
    object BottomSheetButtonClicked : RootAction
}

class RootStateMachine @Inject constructor(
    private val navigator: RootNavigator,
) : StateMachine<RootState, RootAction> {
    private val _state = MutableStateFlow(Init)
    override val state: Flow<RootState> = _state

    override suspend fun dispatch(action: RootAction) {
        when (action) {
            RootAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
        }
    }
}
