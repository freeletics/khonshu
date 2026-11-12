package com.freeletics.khonshu.navigation

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.activity.sendResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class TestHostNavigatorPlatformNavigatorTest {
    @Test
    fun `permission navigation is recorded`() = runTest {
        val platformNavigator = TestHostNavigator().platformNavigator()
        val navigator = PermissionNavigator(platformNavigator)

        (platformNavigator as DestinationNavigator).test {
            navigator.requestPermission()

            awaitRequestPermissions(navigator.request, "permission")
        }
    }

    @Test
    fun `activity navigation is recorded with testIn`() = runTest {
        val platformNavigator = TestHostNavigator().platformNavigator()
        val navigator = ResultNavigator(platformNavigator)

        turbineScope {
            val events = (platformNavigator as DestinationNavigator).testIn(CoroutineScope(currentCoroutineContext()))
            navigator.open()

            events.awaitNavigateForResult(navigator.request, "input")
            events.cancel()
        }
    }

    @Test
    fun `permission results are delivered to the injected navigator`() = runTest {
        val navigator = PermissionNavigator(TestHostNavigator().platformNavigator())

        navigator.request.results.test {
            navigator.request.sendResult("permission", PermissionResult.Granted)

            assertThat(awaitItem()).containsExactly("permission", PermissionResult.Granted)
        }
    }

    @Test
    fun `activity results are delivered to the injected navigator`() = runTest {
        val navigator = ResultNavigator(TestHostNavigator().platformNavigator())

        navigator.request.results.test {
            navigator.request.sendResult("result")

            assertThat(awaitItem()).isEqualTo("result")
        }
    }

    @Test
    fun `platform navigator observes events from its host`() = runTest {
        val hostNavigator = TestHostNavigator()
        val platformNavigator = hostNavigator.platformNavigator()

        (platformNavigator as DestinationNavigator).test {
            hostNavigator.navigateBack()

            awaitNavigateBack()
        }
    }

    private class PermissionNavigator(private val navigator: PlatformNavigator) {
        val request = navigator.registerForPermissionsResult()

        fun requestPermission() {
            navigator.requestPermissions(request, "permission")
        }
    }

    private class ResultNavigator(private val navigator: PlatformNavigator) {
        val request = navigator.registerForActivityResult(TestContract())

        fun open() {
            navigator.navigateForResult(request, "input")
        }
    }

    private class TestContract : ActivityResultContract<String, String>() {
        override fun createIntent(context: Context, input: String): Intent = error("Not needed in this test")

        override fun parseResult(resultCode: Int, intent: Intent?): String = error("Not needed in this test")
    }
}
