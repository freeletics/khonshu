package com.freeletics.mad.example.feature.main

import com.freeletics.mad.statemachine.StateMachine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

sealed interface MainState

object Init : MainState

sealed interface MainAction

class MainStateMachine @Inject constructor() : StateMachine<MainState, MainAction> {
    override val state: Flow<MainState> = flowOf(Init)

    override suspend fun dispatch(action: MainAction) {}
}
