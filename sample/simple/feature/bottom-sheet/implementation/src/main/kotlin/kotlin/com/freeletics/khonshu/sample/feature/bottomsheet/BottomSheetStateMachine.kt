package com.freeletics.khonshu.sample.feature.bottomsheet

import com.freeletics.khonshu.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

sealed interface BottomSheetState

data object Init : BottomSheetState

sealed interface BottomSheetAction {
    data object DismissRequested : BottomSheetAction
}

class BottomSheetStateMachine @Inject constructor(
    private val navigator: BottomSheetNavigator,
) : StateMachine<BottomSheetState, BottomSheetAction> {
    override val state: Flow<BottomSheetState> = flowOf(Init)

    override suspend fun dispatch(action: BottomSheetAction) {
        when (action) {
            BottomSheetAction.DismissRequested -> navigator.navigateBack()
        }
    }
}
