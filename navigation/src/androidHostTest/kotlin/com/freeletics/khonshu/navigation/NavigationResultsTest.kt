package com.freeletics.khonshu.navigation

import app.cash.turbine.test
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackEntryState
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestParcelable
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.serializer
import org.junit.Test

internal class NavigationResultsTest {
    @Test
    fun registerNavigationResults(): Unit = runBlocking {
        val navigator = TestHostNavigator()
        val request = navigator.registerForNavigationResult<SimpleRoute, TestParcelable>()

        request.results.test {
            request.sendResult(TestParcelable(4))
            assertThat(awaitItem()).isEqualTo(TestParcelable(4))
        }
    }

    @Test
    fun deliverNavigationResult() = runTest {
        val navigator = TestHostNavigator()
        val key = fakeNavigationResultKey<TestParcelable>()

        navigator.test {
            navigator.deliverNavigationResult(key, TestParcelable(5))

            awaitNavigationResult(key, TestParcelable(5))
        }
    }

    @Test
    fun `NavigationResultRequest emits events`(): Unit = runBlocking {
        val owner = NavigationResultRequest(
            NavigationResultRequest.Key(StackEntry.Id(""), "key"),
            StackEntryState(),
            serializer<SimpleRoute>(),
        )

        owner.results.test {
            owner.sendResult(SimpleRoute(1))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))

            owner.sendResult(SimpleRoute(2))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(2))

            owner.sendResult(SimpleRoute(3))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(3))
        }
    }

    @Test
    fun `NavigationResultRequest emits results that were delivered before collection`(): Unit = runBlocking {
        val owner = NavigationResultRequest(
            NavigationResultRequest.Key(StackEntry.Id(""), "key"),
            StackEntryState(),
            serializer<SimpleRoute>(),
        )

        owner.sendResult(SimpleRoute(1))

        owner.results.test {
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))
        }
    }

    @Test
    fun `NavigationResultRequest emits results were delivered before and during collection`(): Unit = runBlocking {
        val owner = NavigationResultRequest(
            NavigationResultRequest.Key(StackEntry.Id(""), "key"),
            StackEntryState(),
            serializer<SimpleRoute>(),
        )

        owner.sendResult(SimpleRoute(1))
        owner.results.test {
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))

            owner.sendResult(SimpleRoute(2))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(2))
        }
    }
}
