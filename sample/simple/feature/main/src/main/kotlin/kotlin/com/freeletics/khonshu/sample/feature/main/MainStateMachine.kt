package com.freeletics.khonshu.sample.feature.main

import com.freeletics.khonshu.statemachine.StateMachine
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

sealed interface MainState

data object Init : MainState

sealed interface MainAction

class MainStateMachine @Inject constructor() : StateMachine<MainState, MainAction> {
    override val state: Flow<MainState> = flowOf(Init)

    override suspend fun dispatch(action: MainAction) {}
}
