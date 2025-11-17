package com.freeletics.khonshu.navigation.activity

import app.cash.turbine.test
import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest.PermissionResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class ActivityResultContractRequestTest {
    @Test
    fun `ActivityResultContractRequest emits events`(): Unit = runBlocking {
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
    fun `ActivityResultContractRequest emits results that were delivered before collection`(): Unit = runBlocking {
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
    fun `ActivityResultContractRequest emits results were delivered before and during collection`(): Unit = runBlocking {
        val owner = PermissionsResultRequest()

        owner.onResult(mapOf("a" to PermissionResult.Granted))
        owner.results.test {
            assertThat(awaitItem()).isEqualTo(mapOf("a" to PermissionResult.Granted))

            owner.onResult(mapOf("b" to PermissionResult.Denied(true)))
            assertThat(awaitItem()).isEqualTo(mapOf("b" to PermissionResult.Denied(true)))
        }
    }
}
