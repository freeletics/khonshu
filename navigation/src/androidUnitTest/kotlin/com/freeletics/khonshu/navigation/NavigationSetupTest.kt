package com.freeletics.khonshu.navigation

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.Turbine
import app.cash.turbine.test
import com.freeletics.khonshu.navigation.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.test.SimpleActivity
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestActivityNavigator
import com.freeletics.khonshu.navigation.test.TestActivityResultLauncher
import com.freeletics.khonshu.navigation.test.TestHostNavigator
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

internal class NavigationSetupTest {
    private val navigator = TestActivityNavigator()
    private val hostNavigator = TestHostNavigator()
    private val activityRequest = navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
    private val activityRequest2 = navigator.testRegisterForActivityResult(ActivityResultContracts.TakePicture())
    private val activityLauncher = TestActivityResultLauncher()
    private val permissionRequest = navigator.testRegisterForPermissionResult()
    private val permissionLauncher = TestActivityResultLauncher()
    private val launchers = mapOf<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>(
        activityRequest to activityLauncher,
        permissionRequest to permissionLauncher,
    )

    private val started = Turbine<Pair<ActivityRoute, NavRoute?>>()
    private val activityStarter: (ActivityRoute, NavRoute?) -> Unit = { route, fallback ->
        started.add(route to fallback)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val testLifecycleOwner = TestLifecycleOwner(RESUMED, dispatcher)
    private val lifecyle = testLifecycleOwner.lifecycle

    @Before
    @OptIn(ExperimentalCoroutinesApi::class)
    fun prepare() {
        Dispatchers.setMain(dispatcher)
    }

    private fun setup() {
        CoroutineScope(dispatcher).launch {
            navigator.collectAndHandleNavEvents(lifecyle, hostNavigator, activityStarter, launchers)
        }
    }

    @After
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tearDown() = runBlocking {
        hostNavigator.received.cancel()
        activityLauncher.launched.cancel()
        permissionLauncher.launched.cancel()
        started.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `does not drop events when paused`() = runBlocking {
        // send events before setup
        repeat(1000) {
            navigator.navigateTo(SimpleActivity(it))
        }
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

        setup()

        // receive events on resume
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertThat(List(1000) { started.awaitItem() })
            .containsExactlyElementsIn(List(1000) { SimpleActivity(it) to null })
            .inOrder()

        // send events on paused
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        repeat(1000) {
            navigator.navigateTo(SimpleActivity(1000 + it))
        }

        // receive events on resume
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertThat(List(1000) { started.awaitItem() })
            .containsExactlyElementsIn(List(1000) { SimpleActivity(1000 + it) to null })
            .inOrder()
    }

    @Test
    fun `NavigateToActivityEvent is forwarded to activity starter`() = runBlocking {
        setup()

        navigator.navigateTo(SimpleActivity(1), SimpleRoute(1))
        assertThat(started.awaitItem()).isEqualTo(SimpleActivity(1) to SimpleRoute(1))
    }

    @Test
    fun `ActivityResultEvent is forwarded to launcher`() = runBlocking {
        setup()

        navigator.navigateForResult(activityRequest, "abc")
        assertThat(activityLauncher.launched.awaitItem()).isEqualTo("abc")
    }

    @Test
    fun `ActivityResultEvent for permissions is forwarded to launcher`() = runBlocking {
        setup()

        navigator.requestPermissions(permissionRequest, "def")
        assertThat(permissionLauncher.launched.awaitItem()).isEqualTo(listOf("def"))
    }

    @Test
    fun `ActivityResultEvent throws exception if no launcher was registered`() = runBlocking {
        navigator.navigateForResult(activityRequest, "")
        val exception = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                val launchers = mapOf<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>(
                    permissionRequest to permissionLauncher,
                )
                navigator.collectAndHandleNavEvents(lifecyle, hostNavigator, activityStarter, launchers)
            }
        }
        assertThat(exception).hasMessageThat().isEqualTo(
            "No launcher registered for request with contract ${activityRequest.contract}!\n" +
                "Make sure you called the appropriate ActivityNavigator.registerFor... method",
        )
    }

    @Test
    fun `deliverResult forwards results`() = runBlocking {
        activityRequest2.deliverResult(true) {
            throw AssertionError("Should not be called")
        }

        activityRequest2.results.test {
            assertThat(awaitItem()).isEqualTo(true)

            activityRequest2.deliverResult(false) {
                throw AssertionError("Should not be called")
            }
            assertThat(awaitItem()).isEqualTo(false)
        }
    }

    @Test
    fun `deliverResult for permissions forwards results`() = runBlocking {
        permissionRequest.deliverResult(mapOf("a" to true)) { throw AssertionError("Should not be called") }

        permissionRequest.results.test {
            assertThat(awaitItem()).isEqualTo(mapOf("a" to PermissionResult.Granted))

            permissionRequest.deliverResult(mapOf("a" to true, "b" to true)) {
                throw AssertionError("Should not be called")
            }
            assertThat(awaitItem()).isEqualTo(
                mapOf(
                    "a" to PermissionResult.Granted,
                    "b" to PermissionResult.Granted,
                ),
            )

            permissionRequest.deliverResult(mapOf("a" to false, "b" to false, "c" to true)) {
                when (it) {
                    "a" -> true
                    "b" -> false
                    else -> throw AssertionError("Not allowed permission $it")
                }
            }
            assertThat(awaitItem()).isEqualTo(
                mapOf(
                    "a" to PermissionResult.Denied(true),
                    "b" to PermissionResult.Denied(false),
                    "c" to PermissionResult.Granted,
                ),
            )
        }
    }
}
