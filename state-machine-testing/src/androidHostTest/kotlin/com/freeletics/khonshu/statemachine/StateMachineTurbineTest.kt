package com.freeletics.khonshu.statemachine

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StateMachineTurbineTest {
    val stateMachine = object : StateMachine<String, Int> {
        private val mutableState = MutableStateFlow<String>("")
        override val state: Flow<String>
            get() = mutableState

        override suspend fun dispatch(action: Int) {
            mutableState.value += action
        }

    }

    @Test
    fun test() = runTest {
        stateMachine.test {
            assertThat(awaitState()).isEqualTo("")
            dispatch(1)
            assertThat(awaitState()).isEqualTo("1")
            dispatch(2)
            dispatch(3)
            assertThat(awaitState()).isEqualTo("12")
            assertThat(awaitState()).isEqualTo("123")
        }
    }
}
