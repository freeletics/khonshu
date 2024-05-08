package com.freeletics.mad.statemachine

import com.freeletics.khonshu.statemachine.StateMachine as KhonshuStateMachine
import kotlinx.coroutines.flow.Flow

/**
 * A state machine that emits [State] objects through the [Flow] returned by [state]. The state
 * can be mutated through actions passed to [dispatch].
 */
public interface StateMachine<State : Any, Action : Any> : KhonshuStateMachine<State, Action> {

    /**
     * A [Flow] that emits the current state as well as all changes to the state.
     */
    public override val state: Flow<State>

    /**
     * An an [Action] to the [KhonshuStateMachine]. The implementation can mutate the [State] based on
     * these actions or trigger side effects.
     */
    public override suspend fun dispatch(action: Action)
}
