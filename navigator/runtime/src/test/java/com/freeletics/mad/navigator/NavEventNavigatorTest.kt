package com.freeletics.mad.navigator

import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import app.cash.turbine.test
import com.freeletics.mad.navigator.NavEvent.NavigateToActivityEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToRootEvent
import com.freeletics.mad.navigator.internal.DestinationId
import com.google.common.truth.Truth.assertThat
import dev.drewhamilton.poko.Poko
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertThrows
import org.junit.Test

public class NavEventNavigatorTest {

    private class TestNavigator : NavEventNavigator() {
        fun <I, O> testRegisterForActivityResult(contract: ActivityResultContract<I, O>): ActivityResultRequest<I, O> {
            return registerForActivityResult(contract)
        }

        fun testRegisterForPermissionResult(): PermissionsResultRequest {
            return registerForPermissionsResult()
        }

        inline fun <reified I : BaseRoute, reified O : Parcelable> testRegisterForNavigationResult(): NavigationResultRequest<O> {
            return registerForNavigationResult<I, O>()
        }
    }

    @Poko
    @Parcelize
    private class SimpleRoute(val number: Int) : NavRoute
    @Poko
    @Parcelize
    private class OtherRoute(val number: Int) : NavRoute
    @Poko
    @Parcelize
    private class SimpleRoot(val number: Int) : NavRoot
    @Poko
    @Parcelize
    private class SimpleActivity(val number: Int) : ActivityRoute, Parcelable
    @Poko
    @Parcelize
    private class TestParcelable(val value: Int) : Parcelable

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
    public fun `navigateToRoot event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateToRoot(SimpleRoot(1), true)

            assertThat(awaitItem()).isEqualTo(NavigateToRootEvent(SimpleRoot(1), true))

            cancel()
        }
    }

    @Test
    public fun `navigateTo Activity event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            navigator.navigateTo(SimpleActivity(1))

            assertThat(awaitItem()).isEqualTo(NavigateToActivityEvent(SimpleActivity(1)))

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

            assertThat(awaitItem()).isEqualTo(
                NavEvent.BackToEvent(DestinationId(SimpleRoute::class), true)
            )

            cancel()
        }
    }

    @Test
    public fun `navigateForResult event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
            navigator.navigateForResult(launcher, "image/*")

            assertThat(awaitItem()).isEqualTo(NavEvent.ActivityResultEvent(launcher, "image/*"))

            cancel()
        }
    }

    @Test
    public fun `requestPermissions event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForPermissionResult()
            val permission = "android.permission.READ_CALENDAR"
            navigator.requestPermissions(launcher, permission)

            assertThat(awaitItem()).isEqualTo(NavEvent.PermissionsResultEvent(launcher, listOf(permission)))

            cancel()
        }
    }

    @Test
    public fun `deliverResult event is received`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navEvents.test {
            val launcher = navigator.testRegisterForNavigationResult<SimpleRoute, TestParcelable>()
            navigator.deliverNavigationResult(launcher.key, TestParcelable(1))

            assertThat(awaitItem()).isEqualTo(
                NavEvent.DestinationResultEvent(launcher.key, TestParcelable(1)))

            cancel()
        }
    }

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
            navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
        }
        assertThat(exception).hasMessageThat().isEqualTo("Failed to register for " +
            "result! You must call this before this navigator gets attached to a " +
            "fragment, e.g. during initialisation of your navigator subclass.")
    }

    @Test
    public fun `registerForPermissionsResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.activityResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.testRegisterForPermissionResult()
        }
        assertThat(exception).hasMessageThat().isEqualTo("Failed to register for " +
            "result! You must call this before this navigator gets attached to a " +
            "fragment, e.g. during initialisation of your navigator subclass.")
    }

    @Test
    public fun `registerForNavigationResult after read is disallowed`(): Unit = runBlocking {
        val navigator = TestNavigator()

        navigator.navigationResultRequests

        val exception = assertThrows(IllegalStateException::class.java) {
            navigator.testRegisterForNavigationResult<SimpleRoute, TestParcelable>()
        }
        assertThat(exception).hasMessageThat().isEqualTo("Failed to register for " +
            "result! You must call this before this navigator gets attached to a " +
            "fragment, e.g. during initialisation of your navigator subclass.")
    }
}
