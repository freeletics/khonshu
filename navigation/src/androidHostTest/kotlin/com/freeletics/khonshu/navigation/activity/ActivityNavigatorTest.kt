package com.freeletics.khonshu.navigation.activity

import androidx.activity.result.contract.ActivityResultContracts
import app.cash.turbine.test
import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent.NavigateForResult
import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent.NavigateTo
import com.freeletics.khonshu.navigation.test.SimpleActivity
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestActivityNavigator
import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class ActivityNavigatorTest {
    @Test
    fun `navigateTo event is received`(): Unit = runBlocking {
        val navigator = TestActivityNavigator()

        navigator.activityEvents.test {
            navigator.navigateTo(SimpleActivity(1), SimpleRoute(2))

            Truth.assertThat(awaitItem())
                .isEqualTo(NavigateTo(SimpleActivity(1), SimpleRoute(2)))

            cancel()
        }
    }

    @Test
    fun `navigateForResult event is received`(): Unit = runBlocking {
        val navigator = TestActivityNavigator()

        navigator.activityEvents.test {
            val launcher =
                navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
            navigator.navigateForResult(launcher, "image/*")

            Truth.assertThat(awaitItem())
                .isEqualTo(NavigateForResult(launcher, "image/*"))

            cancel()
        }
    }

    @Test
    fun `requestPermissions event is received`(): Unit = runBlocking {
        val navigator = TestActivityNavigator()

        navigator.activityEvents.test {
            val launcher = navigator.testRegisterForPermissionResult()
            val permission = "android.permission.READ_CALENDAR"
            navigator.requestPermissions(launcher, permission)

            Truth.assertThat(awaitItem())
                .isEqualTo(NavigateForResult(launcher, listOf(permission)))

            cancel()
        }
    }

    @Test
    fun `registerForActivityResult after read is allowed`(): Unit = runBlocking {
        val navigator = TestActivityNavigator()

        navigator.activityResultRequests

        val request = navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())

        Truth.assertThat(navigator.activityResultRequests).containsExactly(request)
    }

    @Test
    fun `registerForPermissionsResult after read is allowed`(): Unit = runBlocking {
        val navigator = TestActivityNavigator()

        navigator.activityResultRequests

        val request = navigator.testRegisterForPermissionResult()

        Truth.assertThat(navigator.activityResultRequests).containsExactly(request)
    }

    @Test
    fun `activityResultRequests reflects requests that are registered later`(): Unit = runBlocking {
        val navigator = TestActivityNavigator()

        val requests = navigator.activityResultRequests
        Truth.assertThat(requests).isEmpty()

        val first = navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
        val second = navigator.testRegisterForPermissionResult()

        // the previously obtained list is a live view, so that a composition observing it is
        // recomposed and can create a launcher for the new requests
        Truth.assertThat(requests).containsExactly(first, second).inOrder()
    }
}
