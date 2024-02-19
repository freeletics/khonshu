package com.freeletics.khonshu.codegen.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.awaitComplete
import app.cash.turbine.awaitItem
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class CollectAsStateTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun prepare() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `upstream is immediately collected when state is CREATED`() = runTest {
        val test = TestLifecycleOwner(initialState = Lifecycle.State.CREATED)
        val upstream = flowOf(0, 1, 2)

        upstream.runUntilDownEvent(test.lifecycle, Lifecycle.State.RESUMED).test {
            assertThat(awaitItem()).isEqualTo(0)
            assertThat(awaitItem()).isEqualTo(1)
            assertThat(awaitItem()).isEqualTo(2)
            awaitComplete()
        }
    }

    @Test
    fun `upstream is immediately collected when state is RESUMED`() = runTest {
        val test = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)
        val upstream = flowOf(0, 1, 2)

        upstream.runUntilDownEvent(test.lifecycle, Lifecycle.State.RESUMED).test {
            assertThat(awaitItem()).isEqualTo(0)
            assertThat(awaitItem()).isEqualTo(1)
            assertThat(awaitItem()).isEqualTo(2)
            awaitComplete()
        }
    }

    @Test
    fun `upstream waits for created when state is INITIALIZED`() = runTest {
        val test = TestLifecycleOwner(initialState = Lifecycle.State.INITIALIZED)
        val upstream = flowOf(0, 1, 2)

        upstream.runUntilDownEvent(test.lifecycle, Lifecycle.State.RESUMED).test {
            expectNoEvents()

            test.setCurrentState(Lifecycle.State.CREATED)
            assertThat(awaitItem()).isEqualTo(0)
            assertThat(awaitItem()).isEqualTo(1)
            assertThat(awaitItem()).isEqualTo(2)
            awaitComplete()
        }
    }

    @Test
    fun `immediately cancels when state is DESTROYED`() = runTest {
        val test = TestLifecycleOwner(initialState = Lifecycle.State.CREATED)
        test.setCurrentState(Lifecycle.State.DESTROYED)
        val upstream = flowOf(0, 1, 2)

        upstream.runUntilDownEvent(test.lifecycle, Lifecycle.State.RESUMED).test {
            awaitComplete()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `completes on down event`() = runTest {
        val test = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)
        val upstream = Channel<Int>(capacity = Int.MAX_VALUE)
        upstream.send(0)

        upstream.receiveAsFlow().runUntilDownEvent(test.lifecycle, Lifecycle.State.RESUMED).test {
            assertThat(awaitItem()).isEqualTo(0)

            test.setCurrentState(Lifecycle.State.STARTED)
            upstream.send(1)
            awaitComplete()
        }

        // 1 was never collected
        assertThat(upstream.awaitItem()).isEqualTo(1)
        assertThat(upstream.isEmpty).isEqualTo(true)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `completes on down event and ignores send failure`() = runTest {
        val test = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        val upstream = Channel<Int>(capacity = Int.MAX_VALUE)
        val wait = Channel<Int>()

        launch {
            upstream.send(0)
            // wait for state to change from RESUMED to STARTED
            test.lifecycle.currentStateFlow.first { it == Lifecycle.State.STARTED }
            // send next item after the stat changed so that flow should get cancelled by now
            upstream.send(1)
            // close the channel so that awaitComplete returns and the 1 can be collected
            wait.close()
        }

        upstream.receiveAsFlow().runUntilDownEvent(test.lifecycle, Lifecycle.State.RESUMED).collect {
            // asserts that only the 0 is collected here
            assertThat(it).isEqualTo(0)
            // when receiving first item trigger a down event to stop the collection
            test.setCurrentState(Lifecycle.State.STARTED)
            // then suspend so that the Flow is not cancelled before the next item is sent
            wait.awaitComplete()
        }

        // The 1 was consumed from the channel, but because of the assert inside collect never received
        // it. This means it triggered a ClosedSendChannelException which was ignored.
        assertThat(upstream.isEmpty).isEqualTo(true)
    }
}
