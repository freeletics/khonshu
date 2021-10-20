package com.freeletics.mad.statemachine

import kotlinx.coroutines.flow.Flow

/**
 * A state machine that emits [State] objects through the [Flow] returned by [state]. The state
 * can be mutated through actions passed to [dispatch].
 */
interface StateMachine<State : Any, Action : Any> {

    /**
     * A [Flow] that emits the current state as well as all changes to the state.
     */
    val state: Flow<State>

    /**
     * An an [Action] to the [StateMachine]. The implementation can mutate the [State] based on
     * these actions or trigger side effects.
     */
    suspend fun dispatch(action: Action)
}


