package com.freeletics.khonshu.navigation

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import app.cash.turbine.testIn
import com.freeletics.khonshu.navigation.activity.ActivityResultRequest
import com.freeletics.khonshu.navigation.activity.ActivityRoute
import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest
import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent
import com.google.common.truth.Truth
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

/**
 * Collects events from [TestHostNavigator] and allows the [validate] lambda to consume
 * and assert properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public suspend fun TestHostNavigator.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend NavigatorTurbine.() -> Unit,
) {
    events.test(timeout, name) {
        val turbine = DefaultNavigatorTurbine(this, savedStateHandle, this)
        validate(turbine)
    }
}

/**
 * Collects events from [DestinationNavigator] and allows the [validate] lambda to consume
 * and assert properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * Note: This requires passing [TestHostNavigator] to [DestinationNavigator].
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public suspend fun DestinationNavigator.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend NavigatorTurbine.() -> Unit,
) {
    val testHostNavigator = hostNavigator as TestHostNavigator
    val hostEvents = testHostNavigator.events
    val activityEvents = activityEvents.map(::toTestEvent)
    merge(hostEvents, activityEvents).test(timeout, name) {
        val turbine = DefaultNavigatorTurbine(this, testHostNavigator.savedStateHandle, this)
        validate(turbine)
    }
}

/**
 * Collects events from [TestHostNavigator] and returns a [NavigatorTurbine] for consuming
 * and asserting properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * Unlike [test] which automatically cancels the flow at the end of the lambda, the returned
 * NavigatorTurbine be explicitly canceled.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public fun TestHostNavigator.testIn(
    scope: CoroutineScope,
    timeout: Duration? = null,
    name: String? = null,
): NavigatorTurbine {
    val turbine = events.testIn(scope, timeout, name)
    return DefaultNavigatorTurbine(turbine, savedStateHandle, scope)
}

/**
 * Collects events from [DestinationNavigator] and returns a [NavigatorTurbine] for consuming
 * and asserting properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * Unlike [test] which automatically cancels the flow at the end of the lambda, the returned
 * NavigatorTurbine be explicitly canceled.
 *
 * Note: This requires passing [TestHostNavigator] to [DestinationNavigator].
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public fun DestinationNavigator.testIn(
    scope: CoroutineScope,
    timeout: Duration? = null,
    name: String? = null,
): NavigatorTurbine {
    val testHostNavigator = hostNavigator as TestHostNavigator
    val hostEvents = testHostNavigator.events
    val activityEvents = activityEvents.map(::toTestEvent)
    val turbine = merge(hostEvents, activityEvents).testIn(scope, timeout, name)
    return DefaultNavigatorTurbine(turbine, testHostNavigator.savedStateHandle, scope)
}

public interface NavigatorTurbine {
    /**
     * Assert that the next event received was a navigation event to the given [route]. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateTo(route: NavRoute)

    /**
     * Assert that the next event received was a navigation event to the given [route]. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateTo(route: ActivityRoute, fallbackRoute: NavRoute? = null)

    /**
     * Assert that all the events in the [block] are received. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigate(block: Navigator.() -> Unit)

    /**
     * Assert that the next event received was an "up" navigation event. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateUp()

    /**
     * Assert that the next event received was a back navigation event. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateBack()

    /**
     * Assert that the next event received was a "back to" navigation event with matching parameters.
     * This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun <T : NavRoute> awaitNavigateBackTo(
        popUpTo: KClass<T>,
        inclusive: Boolean,
    )

    /**
     * Assert that the next event received was a "switch back stack" navigation event with matching
     * parameters. This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitSwitchBackStack(root: NavRoot)

    /**
     * Assert that the next event received was a "show root" navigation event with matching
     * parameters. This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitShowRoot(root: NavRoot)

    /**
     * Assert that the next event received was a "replace all back stacks" navigation event with matching
     * parameters. This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitReplaceAllBackStacks(root: NavRoot)

    /**
     * Assert that the next event received was a navigate for result event to [request].
     * This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateForResult(request: ActivityResultRequest<Void?, *>)

    /**
     * Assert that the next event received was a navigate for result event to [request].
     * This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun <I> awaitNavigateForResult(request: ActivityResultRequest<I, *>, input: I)

    /**
     * Assert that the next event received was a permission request. This function will suspend
     * if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitRequestPermissions(
        request: PermissionsResultRequest,
        vararg permissions: String,
    )

    /**
     * Assert that the next event received was a permission request. This function will suspend
     * if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitRequestPermissions(
        request: PermissionsResultRequest,
        permissions: List<String>,
    )

    /**
     * Assert that the next event received was a navigation result. This function will suspend
     * if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun <O : Parcelable> awaitNavigationResult(
        key: NavigationResultRequest.Key<O>,
        result: O,
    )

    /**
     * Cancel this [NavigatorTurbine].
     *
     * @throws AssertionError - if there are any unconsumed events.
     */
    public suspend fun cancel()

    /**
     * Cancel this [NavigatorTurbine]. The difference to [cancel] is any unconsumed event will be
     * ignored and no error will be thrown.
     */
    public suspend fun cancelAndIgnoreRemainingNavEvents()
}

internal class DefaultNavigatorTurbine(
    private val turbine: ReceiveTurbine<TestEvent>,
    private val savedStateHandle: SavedStateHandle,
    private val scope: CoroutineScope,
) : NavigatorTurbine {
    override suspend fun awaitNavigateTo(route: NavRoute) {
        val event = NavigateToEvent(route)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateTo(route: ActivityRoute, fallbackRoute: NavRoute?) {
        val event = NavigateToActivityEvent(ActivityEvent.NavigateTo(route, fallbackRoute))
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigate(block: Navigator.() -> Unit) {
        val navigator = TestHostNavigator()
        navigator.navigate(block)
        val event = navigator.events.first() as BatchEvent
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateUp() {
        val event = UpEvent
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateBack() {
        val event = BackEvent
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun <T : NavRoute> awaitNavigateBackTo(
        popUpTo: KClass<T>,
        inclusive: Boolean,
    ) {
        val event = BackToEvent(popUpTo, inclusive)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitSwitchBackStack(root: NavRoot) {
        val event = SwitchBackStackEvent(root)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitShowRoot(root: NavRoot) {
        val event = ShowRootEvent(root)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitReplaceAllBackStacks(root: NavRoot) {
        val event = ReplaceAllBackStacksEvent(root)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateForResult(request: ActivityResultRequest<Void?, *>) {
        awaitNavigateForResult(request, null)
    }

    override suspend fun <I> awaitNavigateForResult(
        request: ActivityResultRequest<I, *>,
        input: I,
    ) {
        val event = ActivityResultEvent(ActivityEvent.NavigateForResult(request, input))
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitRequestPermissions(
        request: PermissionsResultRequest,
        vararg permissions: String,
    ) {
        awaitRequestPermissions(request, permissions.toList())
    }

    override suspend fun awaitRequestPermissions(
        request: PermissionsResultRequest,
        permissions: List<String>,
    ) {
        val event = ActivityResultEvent(ActivityEvent.NavigateForResult(request, permissions))
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun <O : Parcelable> awaitNavigationResult(
        key: NavigationResultRequest.Key<O>,
        result: O,
    ) {
        val turbine = savedStateHandle.getStateFlow<O?>(key.requestKey, null)
            .filterNotNull()
            .testIn(scope)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(result)
        turbine.cancel()
    }

    override suspend fun cancel() {
        turbine.cancel()
    }

    override suspend fun cancelAndIgnoreRemainingNavEvents() {
        turbine.cancelAndIgnoreRemainingEvents()
    }
}
