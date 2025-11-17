package com.freeletics.khonshu.statemachine

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import app.cash.turbine.testIn
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope

/**
 * Collects `state` from [StateMachine] and and allows the [validate] lambda to consume
 * and assert properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public suspend fun <State : Any, Action : Any> StateMachine<State, Action>.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend StateMachineTurbine<State, Action>.() -> Unit,
) {
    val stateMachine = this
    state.test(timeout, name) {
        val turbine = DefaultStateMachineTurbine(stateMachine, this)
        validate(turbine)
    }
}

/**
 * Collects `state` from [StateMachine] and returns a [StateMachineTurbine] for consuming
 * and asserting properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * Unlike test which automatically cancels the flow at the end of the lambda, the returned
 * StateMachineTurbine be explicitly canceled.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public fun <State : Any, Action : Any> StateMachine<State, Action>.testIn(
    scope: CoroutineScope,
    timeout: Duration? = null,
    name: String? = null,
): StateMachineTurbine<State, Action> {
    val turbine = state.testIn(scope, timeout, name)
    return DefaultStateMachineTurbine(this, turbine)
}

public interface StateMachineTurbine<State : Any, Action : Any> {
    /**
     * Dispatch an [action] to the [StateMachine] under test.
     */
    public suspend fun dispatch(action: Action)

    /**
     * Assert that the next event received was a new state. This function will suspend
     * if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitState(): State

    /**
     * Cancel this [StateMachineTurbine].
     *
     * @throws AssertionError - if there are any unconsumed events.
     */
    public suspend fun cancel()

    /**
     * Cancel this [StateMachineTurbine]. The difference to [cancel] is any unconsumed event will be
     * ignored and no error will be thrown.
     */
    public suspend fun cancelAndIgnoreRemainingStates()
}

internal class DefaultStateMachineTurbine<State : Any, Action : Any>(
    private val stateMachine: StateMachine<State, Action>,
    private val turbine: ReceiveTurbine<State>,
) : StateMachineTurbine<State, Action> {
    override suspend fun dispatch(action: Action) {
        stateMachine.dispatch(action)
    }

    override suspend fun awaitState(): State {
        return turbine.awaitItem()
    }

    override suspend fun cancel() {
        turbine.cancel()
    }

    override suspend fun cancelAndIgnoreRemainingStates() {
        turbine.cancelAndIgnoreRemainingEvents()
    }
}
