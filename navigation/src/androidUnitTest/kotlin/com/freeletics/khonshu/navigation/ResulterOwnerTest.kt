package com.freeletics.khonshu.navigation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.freeletics.khonshu.navigation.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.simpleRootDestination
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class ResulterOwnerTest {
    @Test
    fun `ContractResultOwner emits events`(): Unit = runBlocking {
        val owner = PermissionsResultRequest()

        owner.results.test {
            owner.onResult(mapOf("a" to PermissionResult.Granted))
            assertThat(awaitItem()).isEqualTo(mapOf("a" to PermissionResult.Granted))

            owner.onResult(mapOf("b" to PermissionResult.Denied(true)))
            assertThat(awaitItem()).isEqualTo(mapOf("b" to PermissionResult.Denied(true)))

            owner.onResult(mapOf("c" to PermissionResult.Denied(false)))
            assertThat(awaitItem()).isEqualTo(mapOf("c" to PermissionResult.Denied(false)))
        }
    }

    @Test
    fun `ContractResultOwner emits results that were delivered before collection`(): Unit = runBlocking {
        val owner = PermissionsResultRequest()

        owner.onResult(mapOf("a" to PermissionResult.Granted))
        owner.onResult(mapOf("b" to PermissionResult.Denied(true)))
        owner.onResult(mapOf("c" to PermissionResult.Denied(false)))

        owner.results.test {
            assertThat(awaitItem()).isEqualTo(mapOf("a" to PermissionResult.Granted))
            assertThat(awaitItem()).isEqualTo(mapOf("b" to PermissionResult.Denied(true)))
            assertThat(awaitItem()).isEqualTo(mapOf("c" to PermissionResult.Denied(false)))
        }
    }

    @Test
    fun `ContractResultOwner emits results were delivered before and during collection`(): Unit = runBlocking {
        val owner = PermissionsResultRequest()

        owner.onResult(mapOf("a" to PermissionResult.Granted))
        owner.results.test {
            assertThat(awaitItem()).isEqualTo(mapOf("a" to PermissionResult.Granted))

            owner.onResult(mapOf("b" to PermissionResult.Denied(true)))
            assertThat(awaitItem()).isEqualTo(mapOf("b" to PermissionResult.Denied(true)))
        }
    }

    @Test
    fun `NavigationResultOwner emits events`(): Unit = runBlocking {
        val handle = SavedStateHandle()
        val owner = NavigationResultRequest(NavigationResultRequest.Key(simpleRootDestination.id, "key"), handle)

        owner.results.test {
            handle["key"] = SimpleRoute(1)
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))

            handle["key"] = SimpleRoute(2)
            assertThat(awaitItem()).isEqualTo(SimpleRoute(2))

            handle["key"] = SimpleRoute(3)
            assertThat(awaitItem()).isEqualTo(SimpleRoute(3))
        }
    }

    @Test
    fun `NavigationResultOwner emits results that were delivered before collection`(): Unit = runBlocking {
        val handle = SavedStateHandle()
        val owner = NavigationResultRequest(NavigationResultRequest.Key(simpleRootDestination.id, "key"), handle)

        handle["key"] = SimpleRoute(1)

        owner.results.test {
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))
        }
    }

    @Test
    fun `NavigationResultOwner emits results were delivered before and during collection`(): Unit = runBlocking {
        val handle = SavedStateHandle()
        val owner = NavigationResultRequest(NavigationResultRequest.Key(simpleRootDestination.id, "key"), handle)

        handle["key"] = SimpleRoute(1)
        owner.results.test {
            assertThat(awaitItem()).isEqualTo(SimpleRoute(1))

            handle["key"] = SimpleRoute(2)
            assertThat(awaitItem()).isEqualTo(SimpleRoute(2))
        }
    }
}
