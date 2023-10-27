package com.freeletics.khonshu.sample.feature.bottomsheet

import com.freeletics.khonshu.sample.feature.bottomsheet.nav.BottomSheetRoute
import com.freeletics.khonshu.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class BottomSheetState(val number: Int)

sealed interface BottomSheetAction {
    object DismissRequested : BottomSheetAction
    object ScreenButtonClicked : BottomSheetAction
    object DialogButtonClicked : BottomSheetAction
    object BottomSheetButtonClicked : BottomSheetAction
}

class BottomSheetStateMachine @Inject constructor(
    route: BottomSheetRoute,
    private val navigator: BottomSheetNavigator,
) : StateMachine<BottomSheetState, BottomSheetAction> {
    private val _state = MutableStateFlow(BottomSheetState(route.number))
    override val state: Flow<BottomSheetState> = _state

    override suspend fun dispatch(action: BottomSheetAction) {
        when (action) {
            BottomSheetAction.DismissRequested -> navigator.navigateBack()
            BottomSheetAction.ScreenButtonClicked -> navigator.navigateToScreen()
            BottomSheetAction.DialogButtonClicked -> navigator.navigateToDialog()
            BottomSheetAction.BottomSheetButtonClicked -> navigator.navigateToBottomSheet()
        }
    }
}
