package com.freeletics.mad.sample.feature.dialog

import com.freeletics.mad.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object DialogState

sealed interface DialogAction

class DialogStateMachine @Inject constructor() : StateMachine<DialogState, DialogAction> {
    private val _state = MutableStateFlow(DialogState)
    override val state: Flow<DialogState> = _state

    override suspend fun dispatch(action: DialogAction) {}
}
