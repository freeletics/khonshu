package com.freeletics.khonshu.navigation

import androidx.activity.result.contract.ActivityResultContracts
import app.cash.turbine.test
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.freeletics.khonshu.navigation.test.TestActivityResultNavigator
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

internal class ActivityResultNavigatorTest {

    @Test
    fun `navigateForResult event is received`(): Unit = runBlocking {
        val navigator = TestActivityResultNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
            navigator.navigateForResult(launcher, "image/*")

            assertThat(awaitItem()).isEqualTo(NavEvent.ActivityResultEvent(launcher, "image/*"))

            cancel()
        }
    }

    @Test
    fun `requestPermissions event is received`(): Unit = runBlocking {
        val navigator = TestActivityResultNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForPermissionResult()
            val permission = "android.permission.READ_CALENDAR"
            navigator.requestPermissions(launcher, permission)

            assertThat(awaitItem()).isEqualTo(NavEvent.ActivityResultEvent(launcher, listOf(permission)))

            cancel()
        }
    }

    @Test
    fun `registerForActivityResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestActivityResultNavigator()

        navigator.activityResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
        }
        assertThat(exception).hasMessageThat().isEqualTo(
            "Failed to register for result! You must call this before NavigationSetup is called with this navigator.",
        )
    }

    @Test
    fun `registerForPermissionsResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestActivityResultNavigator()

        navigator.activityResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.testRegisterForPermissionResult()
        }
        assertThat(exception).hasMessageThat().isEqualTo(
            "Failed to register for result! You must call this before NavigationSetup is called with this navigator.",
        )
    }
}
