package com.freeletics.khonshu.sample.feature.screen.result

import com.freeletics.khonshu.statemachine.StateMachine
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class ScreenWithResultState(val data: String)

sealed interface ScreenWithResultAction {
    data class UpdateResult(val data: String) : ScreenWithResultAction

    data object DeliverResult : ScreenWithResultAction
}

@Inject
class ScreenWithResultStateMachine (
    private val navigator: ScreenWithResultNavigator,
) : StateMachine<ScreenWithResultState, ScreenWithResultAction> {
    private val _state = MutableStateFlow(ScreenWithResultState(data = ""))
    override val state: Flow<ScreenWithResultState> = _state

    override suspend fun dispatch(action: ScreenWithResultAction) {
        when (action) {
            ScreenWithResultAction.DeliverResult -> navigator.deliverResult(_state.value.data)
            is ScreenWithResultAction.UpdateResult -> _state.emit(ScreenWithResultState(action.data))
        }
    }
}
