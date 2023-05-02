package com.test

import android.os.Parcel
import android.view.View
import androidx.viewbinding.ViewBinding
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.statemachine.StateMachine
import kotlinx.coroutines.flow.Flow

public class TestScreen
public class TestClass

public class TestRoute : NavRoute {
    override fun describeContents(): Int = 0
    override fun writeToParcel(p0: Parcel, p1: Int) {}
}

public class TestBinding : ViewBinding {
    override fun getRoot(): View = throw UnsupportedOperationException("Not implemented")
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
