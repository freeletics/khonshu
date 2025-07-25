package com.freeletics.khonshu.sample.feature.dialog

import com.freeletics.khonshu.statemachine.StateMachine
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object DialogState

sealed interface DialogAction {
    data object DismissRequested : DialogAction
}

@Inject
class DialogStateMachine(
    private val navigator: DialogNavigator,
) : StateMachine<DialogState, DialogAction> {
    private val _state = MutableStateFlow(DialogState)
    override val state: Flow<DialogState> = _state

    override suspend fun dispatch(action: DialogAction) {
        when (action) {
            DialogAction.DismissRequested -> navigator.navigateBack()
        }
    }
}
