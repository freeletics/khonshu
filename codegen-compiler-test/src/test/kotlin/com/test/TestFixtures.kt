package com.test

import android.os.Parcel
import com.freeletics.khonshu.codegen.Overlay
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.statemachine.StateMachine
import kotlinx.coroutines.flow.Flow

public class TestScreen
public class TestClass

public class TestRoute : NavRoute {
    override fun describeContents(): Int = 0
    override fun writeToParcel(p0: Parcel, p1: Int) {}
}

public class TestOverlayRoute : NavRoute, Overlay {
    override fun describeContents(): Int = 0
    override fun writeToParcel(p0: Parcel, p1: Int) {}
}

public class TestRoot : NavRoot {
    override fun describeContents(): Int = 0
    override fun writeToParcel(p0: Parcel, p1: Int) {}
}

public class TestStateMachine : FooStateMachine<TestAction, TestState>() {
    override val state: Flow<TestState>
        get() = throw UnsupportedOperationException("Not implemented")

    override suspend fun dispatch(action: TestAction) {
        throw UnsupportedOperationException("Not implemented")
    }
}

public abstract class FooStateMachine<A : Any, S : Any> : StateMachine<S, A>

public object TestAction
public object TestState
