package com.freeletics.mad.navigation

import androidx.activity.result.contract.ActivityResultContracts
import app.cash.turbine.test
import com.freeletics.mad.navigation.internal.DestinationId
import com.freeletics.mad.navigation.internal.NavEvent
import com.freeletics.mad.navigation.internal.NavEvent.NavigateToActivityEvent
import com.freeletics.mad.navigation.internal.NavEvent.NavigateToEvent
import com.freeletics.mad.navigation.internal.NavEvent.NavigateToRootEvent
import com.freeletics.mad.navigation.test.OtherRoute
import com.freeletics.mad.navigation.test.SimpleActivity
import com.freeletics.mad.navigation.test.SimpleRoot
import com.freeletics.mad.navigation.test.SimpleRoute
import com.freeletics.mad.navigation.test.TestNavigator
import com.freeletics.mad.navigation.test.TestParcelable
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

internal class NavEventNavigatorTest {

    @Test
    fun `navigateTo event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateTo(SimpleRoute(1))

            assertThat(awaitItem()).isEqualTo(NavigateToEvent(SimpleRoute(1)))

            cancel()
        }
    }

    @Test
    fun `multiple navigateTo event are received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateTo(SimpleRoute(1))
            navigator.navigateTo(OtherRoute(1))
            navigator.navigateTo(SimpleRoute(2))

            assertThat(awaitItem()).isEqualTo(NavigateToEvent(SimpleRoute(1)))
            assertThat(awaitItem()).isEqualTo(NavigateToEvent(OtherRoute(1)))
            assertThat(awaitItem()).isEqualTo(NavigateToEvent(SimpleRoute(2)))

            cancel()
        }
    }

    @Test
    fun `navigateToRoot event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateToRoot(
                root = SimpleRoot(1),
                restoreRootState = true,
            )

            assertThat(awaitItem()).isEqualTo(
                NavigateToRootEvent(
                    root = SimpleRoot(1),
                    restoreRootState = true,
                ),
            )

            cancel()
        }
    }

    @Test
    fun `navigateTo Activity event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateTo(SimpleActivity(1))

            assertThat(awaitItem()).isEqualTo(NavigateToActivityEvent(SimpleActivity(1)))

            cancel()
        }
    }

    @Test
    fun `navigateUp event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateUp()

            assertThat(awaitItem()).isEqualTo(NavEvent.UpEvent)

            cancel()
        }
    }

    @Test
    fun `navigateBack event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateBack()

            assertThat(awaitItem()).isEqualTo(NavEvent.BackEvent)

            cancel()
        }
    }

    @Test
    fun `navigateBackTo event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateBackTo<SimpleRoute>(true)

            assertThat(awaitItem()).isEqualTo(
                NavEvent.BackToEvent(DestinationId(SimpleRoute::class), true),
            )

            cancel()
        }
    }

    @Test
    fun `resetToRoot event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.resetToRoot(
                root = SimpleRoot(1),
            )

            assertThat(awaitItem()).isEqualTo(
                NavEvent.ResetToRoot(
                    root = SimpleRoot(1),
                ),
            )

            cancel()
        }
    }

    @Test
    fun `navigateForResult event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
            navigator.navigateForResult(launcher, "image/*")

            assertThat(awaitItem()).isEqualTo(NavEvent.ActivityResultEvent(launcher, "image/*"))

            cancel()
        }
    }

    @Test
    fun `requestPermissions event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForPermissionResult()
            val permission = "android.permission.READ_CALENDAR"
            navigator.requestPermissions(launcher, permission)

            assertThat(awaitItem()).isEqualTo(NavEvent.ActivityResultEvent(launcher, listOf(permission)))

            cancel()
        }
    }

    @Test
    fun `deliverResult event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForNavigationResult<SimpleRoute, TestParcelable>()
            navigator.deliverNavigationResult(launcher.key, TestParcelable(1))

            assertThat(awaitItem()).isEqualTo(
                NavEvent.DestinationResultEvent(launcher.key, TestParcelable(1)),
            )

            cancel()
        }
    }

    @Test
    fun `backPresses sends out events`(): Unit = runBlocking {
        val navigator = TestNavigator()

        assertThat(navigator.onBackPressedCallback.isEnabled).isFalse()

        navigator.backPresses().test {
            assertThat(navigator.onBackPressedCallback.isEnabled).isTrue()

            navigator.onBackPressedCallback.handleOnBackPressed()
            navigator.onBackPressedCallback.handleOnBackPressed()
            navigator.onBackPressedCallback.handleOnBackPressed()

            assertThat(awaitItem()).isEqualTo(Unit)
            assertThat(awaitItem()).isEqualTo(Unit)
            assertThat(awaitItem()).isEqualTo(Unit)

            cancel()
        }

        assertThat(navigator.onBackPressedCallback.isEnabled).isFalse()
    }

    @Test
    fun `registerForActivityResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.activityResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
        }
        assertThat(exception).hasMessageThat().isEqualTo(
            "Failed to register for " +
                "result! You must call this before this navigator gets attached to a " +
                "fragment, e.g. during initialisation of your navigator subclass.",
        )
    }

    @Test
    fun `registerForPermissionsResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.activityResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.testRegisterForPermissionResult()
        }
        assertThat(exception).hasMessageThat().isEqualTo(
            "Failed to register for " +
                "result! You must call this before this navigator gets attached to a " +
                "fragment, e.g. during initialisation of your navigator subclass.",
        )
    }

    @Test
    fun `registerForNavigationResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navigationResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.testRegisterForNavigationResult<SimpleRoute, TestParcelable>()
        }
        assertThat(exception).hasMessageThat().isEqualTo(
            "Failed to register for " +
                "result! You must call this before this navigator gets attached to a " +
                "fragment, e.g. during initialisation of your navigator subclass.",
        )
    }
}
