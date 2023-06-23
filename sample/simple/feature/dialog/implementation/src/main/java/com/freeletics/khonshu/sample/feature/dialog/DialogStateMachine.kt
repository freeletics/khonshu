package com.freeletics.khonshu.sample.feature.dialog

import com.freeletics.khonshu.sample.feature.dialog.nav.DialogRoute
import com.freeletics.khonshu.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class DialogState(val number: Int)

sealed interface DialogAction {
    object DismissRequested : DialogAction
    object ScreenButtonClicked : DialogAction
    object DialogButtonClicked : DialogAction
    object BottomSheetButtonClicked : DialogAction
}

class DialogStateMachine @Inject constructor(
    route: DialogRoute,
    private val navigator: DialogNavigator,
) : StateMachine<DialogState, DialogAction> {
    private val _state = MutableStateFlow(DialogState(route.number))
    override val state: Flow<DialogState> = _state

    override suspend fun dispatch(action: DialogAction) {
        when (action) {
            DialogAction.DismissRequested -> navigator.navigateBack()
            DialogAction.ScreenButtonClicked -> navigator.navigateToScreen()
            DialogAction.DialogButtonClicked -> navigator.navigateToDialog()
            DialogAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
        }
    }
}
