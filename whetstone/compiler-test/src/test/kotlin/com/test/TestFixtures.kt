package com.test

import android.os.Parcel
import android.view.View
import androidx.compose.runtime.Composable
import androidx.viewbinding.ViewBinding
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.statemachine.StateMachine
import com.gabrielittner.renderer.ViewRenderer
import com.test.other.TestClass2
import kotlinx.coroutines.flow.Flow

public class TestScreen
public class TestClass

public class TestRoute : NavRoute {
    override fun describeContents(): Int = 0
    override fun writeToParcel(p0: Parcel, p1: Int) {}
}

@Suppress("UNUSED_PARAMETER")
@Composable
public fun Test(
    state: TestState,
    sendAction: (TestAction) -> Unit
) {}

@Suppress("UNUSED_PARAMETER")
@Composable
public fun Test2(
    testClass: TestClass,
    test: TestClass2,
    state: TestState,
    sendAction: (TestAction) -> Unit
) {}

public class TestBinding : ViewBinding {
    override fun getRoot(): View = throw UnsupportedOperationException("Not implemented")
}

public class TestRenderer(view: View) : ViewRenderer<TestState, TestAction>(view) {
    override fun renderToView(state: TestState) {}

    public abstract class Factory : ViewRenderer.Factory<TestBinding, TestRenderer>({ _, _, _ -> TestBinding() })
}

public class TestStateMachine : StateMachine<TestState, TestAction> {
    override val state: Flow<TestState>
        get() = throw UnsupportedOperationException("Not implemented")

    override suspend fun dispatch(action: TestAction) {
        throw UnsupportedOperationException("Not implemented")
    }
}

public object TestAction
public object TestState
