package com.freeletics.khonshu.navigation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import org.junit.Test

internal class NavigationResultRequestTest {
    @Test
    fun `NavigationResultRequest emits events`(): Unit = runBlocking {
        val handle = SavedStateHandle()
        val owner = NavigationResultRequest(
            NavigationResultRequest.Key(StackEntry.Id(""), "key"),
            handle,
            serializer<SimpleRoute>(),
        )

        owner.results.test {
            handle["key"] = NavigationResult(SimpleRoute(1))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))

            handle["key"] = NavigationResult(SimpleRoute(2))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(2))

            handle["key"] = NavigationResult(SimpleRoute(3))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(3))
        }
    }

    @Test
    fun `NavigationResultRequest emits results that were delivered before collection`(): Unit = runBlocking {
        val handle = SavedStateHandle()
        val owner = NavigationResultRequest(
            NavigationResultRequest.Key(StackEntry.Id(""), "key"),
            handle,
            serializer<SimpleRoute>(),
        )

        handle["key"] = NavigationResult(SimpleRoute(1))

        owner.results.test {
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))
        }
    }

    @Test
    fun `NavigationResultRequest emits results were delivered before and during collection`(): Unit = runBlocking {
        val handle = SavedStateHandle()
        val owner = NavigationResultRequest(
            NavigationResultRequest.Key(StackEntry.Id(""), "key"),
            handle,
            serializer<SimpleRoute>(),
        )

        handle["key"] = NavigationResult(SimpleRoute(1))
        owner.results.test {
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))

            handle["key"] = NavigationResult(SimpleRoute(2))
            assertThat(awaitItem()).isEqualTo(SimpleRoute(2))
        }
    }
}
