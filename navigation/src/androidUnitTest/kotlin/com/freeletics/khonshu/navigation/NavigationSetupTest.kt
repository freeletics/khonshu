package com.freeletics.khonshu.navigation

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.turbine.test
import com.freeletics.khonshu.navigation.Navigator.Companion.navigateBackTo
import com.freeletics.khonshu.navigation.PermissionsResultRequest.PermissionResult
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.freeletics.khonshu.navigation.internal.Parcelable
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import com.freeletics.khonshu.navigation.test.SimpleActivity
import com.freeletics.khonshu.navigation.test.SimpleRoot
import com.freeletics.khonshu.navigation.test.SimpleRoute
import com.freeletics.khonshu.navigation.test.TestActivityResultLauncher
import com.freeletics.khonshu.navigation.test.TestHostNavigator
import com.freeletics.khonshu.navigation.test.TestNavEventNavigator
import com.freeletics.khonshu.navigation.test.TestParcelable
import com.freeletics.khonshu.navigation.test.TestStackEntryFactory
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
    private val navigator = TestNavEventNavigator()
    private val hostNavigator = TestHostNavigator()
    private val resultRequest = navigator.testRegisterForNavigationResult<SimpleRoute, TestParcelable>()
    private val activityRequest = navigator.testRegisterForActivityResult(ActivityResultContracts.GetContent())
    private val activityRequest2 = navigator.testRegisterForActivityResult(ActivityResultContracts.TakePicture())
    private val activityLauncher = TestActivityResultLauncher()
    private val permissionRequest = navigator.testRegisterForPermissionResult()
    private val permissionLauncher = TestActivityResultLauncher()
    private val launchers = mapOf<ContractResultOwner<*, *, *>, ActivityResultLauncher<*>>(
        activityRequest to activityLauncher,
        permissionRequest to permissionLauncher,
    )

    private val started = mutableListOf<Pair<ActivityRoute, NavRoute?>>()
    private val activityStarter: (ActivityRoute, NavRoute?) -> Unit = { route, fallback ->
        started.add(route to fallback)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val testLifecycleOwner = TestLifecycleOwner(RESUMED, dispatcher)
    private val lifecyle = testLifecycleOwner.lifecycle

    private val factory = TestStackEntryFactory()

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
        Dispatchers.resetMain()
    }

    @Test
    fun `does not drop events when paused`() = runBlocking {
        // send events before setup
        repeat(1000) {
            navigator.navigateTo(SimpleRoute(it))
        }
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

        setup()

        // receive events on resume
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertThat(List(1000) { hostNavigator.received.awaitItem() })
            .containsExactlyElementsIn(List(1000) { NavEvent.NavigateToEvent(SimpleRoute(it)) })
            .inOrder()

        // send events on paused
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        repeat(1000) {
            navigator.navigateTo(SimpleRoute(1000 + it))
        }

        // receive events on resume
        testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        assertThat(List(1000) { hostNavigator.received.awaitItem() })
            .containsExactlyElementsIn(List(1000) { NavEvent.NavigateToEvent(SimpleRoute(1000 + it)) })
            .inOrder()
    }

    @Test
    fun `NavigateToEvent is forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.navigateTo(SimpleRoute(1))
        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(NavEvent.NavigateToEvent(SimpleRoute(1)))
    }

    @Test
    fun `NavigateToRootEvent is forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.navigateToRoot(
            root = SimpleRoot(2),
            restoreRootState = false,
        )
        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(
                NavEvent.NavigateToRootEvent(
                    root = SimpleRoot(2),
                    restoreRootState = false,
                ),
            )
    }

    @Test
    fun `NavigateToActivityEvent is forwarded to activity starter`() = runBlocking {
        setup()

        navigator.navigateTo(SimpleActivity(1), SimpleRoute(1))
        assertThat(started).containsExactly(SimpleActivity(1) to SimpleRoute(1)).inOrder()
    }

    @Test
    fun `UpEvent is forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.navigateUp()
        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(NavEvent.UpEvent)
    }

    @Test
    fun `BackEvent is forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.navigateBack()
        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(NavEvent.BackEvent)
    }

    @Test
    fun `BackToEvent is forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.navigateBackTo<SimpleRoute>(inclusive = true)
        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(NavEvent.BackToEvent(SimpleRoute::class, inclusive = true))
    }

    @Test
    fun `ResetToRoot is forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.resetToRoot(SimpleRoot(2))
        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(NavEvent.ResetToRoot(SimpleRoot(2)))
    }

    @Test
    fun `ReplaceAll is forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.replaceAll(SimpleRoot(2))
        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(NavEvent.ReplaceAll(SimpleRoot(2)))
    }

    @Test
    fun `MultiNavEvent is handled properly and events are forwarded to hostNavigator`() = runBlocking {
        setup()

        navigator.navigate {
            navigateBackTo<SimpleRoute>(true)
            navigateTo(SimpleRoute(1))
            navigateBack()
        }

        assertThat(hostNavigator.received.awaitItem())
            .isEqualTo(
                NavEvent.MultiNavEvent(
                    listOf(
                        NavEvent.BackToEvent(SimpleRoute::class, inclusive = true),
                        NavEvent.NavigateToEvent(SimpleRoute(1)),
                        NavEvent.BackEvent,
                    ),
                ),
            )
    }

    @Test
    fun `DestinationResultEvent is forwarded to hostNavigator`() = runBlocking {
        setup()

        val entry = factory.create(SimpleRoute(0))
        hostNavigator.snapshot.value = StackSnapshot(listOf(entry), entry)
        entry.savedStateHandle.getStateFlow<Parcelable?>(resultRequest.key.requestKey, null).test {
            assertThat(awaitItem()).isEqualTo(null)

            navigator.deliverNavigationResult(resultRequest.key, TestParcelable(9))
            assertThat(awaitItem()).isEqualTo(TestParcelable(9))
        }
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
                "Make sure you called the appropriate NavEventNavigator.registerFor... method",
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

    @Test
    fun `collectAndHandleNavigationResults forwards results`() = runBlocking {
        val entry = factory.create(SimpleRoute(0))
        hostNavigator.snapshot.value = StackSnapshot(listOf(entry), entry)
        CoroutineScope(dispatcher).launch {
            hostNavigator.collectAndHandleNavigationResults(resultRequest)
        }

        resultRequest.results.test {
            entry.savedStateHandle[resultRequest.key.requestKey] = TestParcelable(3)
            assertThat(awaitItem()).isEqualTo(TestParcelable(3))
        }
    }

    @Test
    fun `collectAndHandleNavigationResults forwards initial value if set`() = runBlocking {
        val entry = factory.create(SimpleRoute(0))
        hostNavigator.snapshot.value = StackSnapshot(listOf(entry), entry)
        CoroutineScope(dispatcher).launch {
            hostNavigator.collectAndHandleNavigationResults(resultRequest)
        }

        entry.savedStateHandle[resultRequest.key.requestKey] = TestParcelable(5)
        resultRequest.results.test {
            assertThat(awaitItem()).isEqualTo(TestParcelable(5))
        }
    }

    @Test
    fun `collectAndHandleNavigationResults does not forward same result twice`() = runBlocking {
        val entry = factory.create(SimpleRoute(0))
        hostNavigator.snapshot.value = StackSnapshot(listOf(entry), entry)
        val job = CoroutineScope(dispatcher).launch {
            hostNavigator.collectAndHandleNavigationResults(resultRequest)
        }

        entry.savedStateHandle[resultRequest.key.requestKey] = TestParcelable(5)
        resultRequest.results.test {
            assertThat(awaitItem()).isEqualTo(TestParcelable(5))

            // restart the collection of navigation results
            job.cancel()
            CoroutineScope(dispatcher).launch {
                hostNavigator.collectAndHandleNavigationResults(resultRequest)
            }

            // waiting for an item fails
            try {
                awaitItem()
                // should never be reached
                assertThat(false).isTrue()
            } catch (e: AssertionError) {
                assertThat(e).hasMessageThat().isEqualTo("No value produced in 3s")
            }

            // new value
            entry.savedStateHandle[resultRequest.key.requestKey] = TestParcelable(7)
            assertThat(awaitItem()).isEqualTo(TestParcelable(7))
        }
    }
}
