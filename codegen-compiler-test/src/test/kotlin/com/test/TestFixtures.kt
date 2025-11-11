package com.test

import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.khonshu.codegen.Overlay
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.statemachine.StateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

public class TestScreen

public class TestClass

public class TestRoute : NavRoute

public class TestOverlayRoute : NavRoute, Overlay

public class TestRoot : NavRoot

public class TestStateMachine : FooStateMachine<TestAction, TestState>() {
    override val state: Flow<TestState>
        get() = throw UnsupportedOperationException("Not implemented")

    override suspend fun dispatch(action: TestAction) {
        throw UnsupportedOperationException("Not implemented")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
public class TestStateMachineFactory : FlowReduxStateMachineFactory<TestState, TestAction>()

public abstract class FooStateMachine<A : Any, S : Any> : StateMachine<S, A>

public object TestAction

public object TestState
