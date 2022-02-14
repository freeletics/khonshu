package com.freeletics.mad.navigator

import androidx.activity.result.contract.ActivityResultContracts
import app.cash.turbine.test
import com.freeletics.mad.navigator.NavEvent.NavigateToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToOnTopOfEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToRootEvent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

public class NavEventNavigatorTest {

    private class TestNavigator : NavEventNavigator()
    private data class SimpleRoute(val number: Int) : NavRoute
    private data class OtherRoute(val number: Int) : NavRoute
    private data class SimpleRoot(val number: Int) : NavRoot

    @Test
    public fun `navigateTo event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateTo(SimpleRoute(1))

            assertThat(awaitItem()).isEqualTo(NavigateToEvent(SimpleRoute(1)))

            cancel()
        }
    }

    @Test
    public fun `multiple navigateTo event are received`(): Unit = runBlocking {
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
    public fun `navigateToOnTopOf event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateToOnTopOf<SimpleRoute>(OtherRoute(1), true)

            assertThat(awaitItem()).isEqualTo(NavigateToOnTopOfEvent(OtherRoute(1), SimpleRoute::class, true))

            cancel()
        }
    }

    @Test
    public fun `navigateToOnTopOfRoot event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateToOnTopOfRoot<SimpleRoot>(OtherRoute(1), true)

            assertThat(awaitItem()).isEqualTo(NavEvent.NavigateToOnTopOfRootEvent(OtherRoute(1), SimpleRoot::class, true))

            cancel()
        }
    }

    @Test
    public fun `navigateToRoot event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateToRoot(SimpleRoot(1), true)

            assertThat(awaitItem()).isEqualTo(NavigateToRootEvent(SimpleRoot(1), true))

            cancel()
        }
    }

    @Test
    public fun `navigateUp event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateUp()

            assertThat(awaitItem()).isEqualTo(NavEvent.UpEvent)

            cancel()
        }
    }

    @Test
    public fun `navigateBack event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateBack()

            assertThat(awaitItem()).isEqualTo(NavEvent.BackEvent)

            cancel()
        }
    }

    @Test
    public fun `navigateBackTo event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateBackTo<SimpleRoute>(true)

            assertThat(awaitItem()).isEqualTo(NavEvent.BackToEvent(SimpleRoute::class, true))

            cancel()
        }
    }

    @Test
    public fun `navigateBack with popUpTo event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateBackToRoot<SimpleRoot>(true)

            assertThat(awaitItem()).isEqualTo(NavEvent.BackToRootEvent(SimpleRoot::class, true))

            cancel()
        }
    }

    @Test
    public fun `navigateForResult event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.registerForActivityResult(ActivityResultContracts.GetContent())
            navigator.navigateForResult(launcher, "image/*")

            assertThat(awaitItem()).isEqualTo(NavEvent.ActivityResultEvent(launcher, "image/*"))

            cancel()
        }
    }

    @Test
    public fun `requestPermissions event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.registerForPermissionsResult()
            val permission = "android.permission.READ_CALENDAR"
            navigator.requestPermissions(launcher, permission)

            assertThat(awaitItem()).isEqualTo(NavEvent.PermissionsResultEvent(launcher, listOf(permission)))

            cancel()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    public fun `backPresses sends out events`(): Unit = runBlocking {
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
    public fun `registerForActivityResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.activityResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.registerForActivityResult(ActivityResultContracts.GetContent())
        }
        assertThat(exception).hasMessageThat().isEqualTo("Failed to register for " +
            "result! You must call this before this navigator gets attached to a " +
            "fragment, e.g. during initialisation of your navigator subclass.")
    }

    @Test
    public fun `registerForPermissionsResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.permissionsResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.registerForPermissionsResult()
        }
        assertThat(exception).hasMessageThat().isEqualTo("Failed to register for " +
            "result! You must call this before this navigator gets attached to a " +
            "fragment, e.g. during initialisation of your navigator subclass.")
    }
}
