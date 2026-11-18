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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

internal class TestHostNavigatorPlatformNavigatorTest {
    @Test
    fun `permission navigation is recorded`() = runTest {
        val platformNavigator = TestHostNavigator().platformNavigator()
        val navigator = PermissionNavigator(platformNavigator)

        platformNavigator.test {
            navigator.requestPermission()

            awaitRequestPermissions(navigator.request, "permission")
        }
    }

    @Test
    fun `activity navigation is recorded with testIn`() = runTest {
        val platformNavigator = TestHostNavigator().platformNavigator()
        val navigator = ResultNavigator(platformNavigator)

        turbineScope {
            val events = platformNavigator.testIn(CoroutineScope(currentCoroutineContext()))
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
    fun `platform navigator does not observe back stack events`() = runTest {
        val hostNavigator = TestHostNavigator()
        val platformNavigator = hostNavigator.platformNavigator()
        val navigator = PermissionNavigator(platformNavigator)

        platformNavigator.test {
            hostNavigator.navigateBack()
            navigator.requestPermission()

            awaitRequestPermissions(navigator.request, "permission")
        }
    }

    @Test
    fun `host and platform navigators can be tested together`() = runTest {
        val hostNavigator = TestHostNavigator()
        val platformNavigator = hostNavigator.platformNavigator()
        val navigator = PermissionNavigator(platformNavigator)

        turbineScope {
            val scope = CoroutineScope(currentCoroutineContext())
            val hostEvents = hostNavigator.testIn(scope)
            val platformEvents = platformNavigator.testIn(scope)

            navigator.requestPermission()
            platformEvents.awaitRequestPermissions(navigator.request, "permission")

            hostNavigator.navigateBack()
            hostNavigator.navigateUp()
            hostEvents.awaitNavigateBack()
            hostEvents.awaitNavigateUp()

            hostEvents.cancel()
            platformEvents.cancel()
        }
    }

    @Test
    fun `test rejects platform navigators not backed by a test host`() {
        val platformNavigator = object : PlatformNavigator() {}

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { platformNavigator.test {} }
        }

        assertThat(exception).hasMessageThat().isEqualTo(
            "PlatformNavigator.test/testIn requires a navigator created by TestHostNavigator.platformNavigator().",
        )
    }

    @Test
    fun `testIn rejects platform navigators not backed by a test host`() = runTest {
        val platformNavigator = object : PlatformNavigator() {}

        val exception = assertThrows(IllegalArgumentException::class.java) {
            platformNavigator.testIn(this)
        }

        assertThat(exception).hasMessageThat().isEqualTo(
            "PlatformNavigator.test/testIn requires a navigator created by TestHostNavigator.platformNavigator().",
        )
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
