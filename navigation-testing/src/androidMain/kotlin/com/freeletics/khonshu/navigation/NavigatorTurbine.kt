package com.freeletics.khonshu.navigation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import app.cash.turbine.testIn
import com.freeletics.khonshu.navigation.activity.ActivityResultRequest
import com.freeletics.khonshu.navigation.activity.ActivityRoute
import com.freeletics.khonshu.navigation.activity.PermissionsResultRequest
import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.google.common.truth.Truth
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * Collects events from [TestHostNavigator] and allows the [validate] lambda to consume
 * and assert properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
@OptIn(InternalNavigationApi::class)
public suspend fun TestHostNavigator.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend HostNavigatorTurbine.() -> Unit,
) {
    events.test(timeout, name) {
        val turbine = DefaultNavigatorTurbine(this, state, this)
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
@Deprecated(
    "Use TestHostNavigator.test for back stack events and PlatformNavigator.test for activity and permission events. " +
        "See the tests in https://freeletics.github.io/khonshu/navigation/migration-destination-navigator2/",
)
@OptIn(InternalNavigationApi::class)
public suspend fun DestinationNavigator.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend NavigatorTurbine.() -> Unit,
) {
    val testHostNavigator = hostNavigator as TestHostNavigator
    merge(testHostNavigator.events, activityTestEvents()).testEvents(testHostNavigator, timeout, name, validate)
}

/**
 * Collects activity and permission events from a navigator created by
 * [TestHostNavigator.platformNavigator]. The [validate] lambda consumes and asserts events in order.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public suspend fun PlatformNavigator.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend PlatformNavigatorTurbine.() -> Unit,
) {
    activityTestEvents().testEvents(testHostNavigator(), timeout, name, validate)
}

/**
 * Collects events from [TestHostNavigator] and returns a [HostNavigatorTurbine] for consuming
 * and asserting properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * Unlike [test] which automatically cancels the flow at the end of the lambda, the returned
 * HostNavigatorTurbine must be explicitly canceled.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
@OptIn(InternalNavigationApi::class)
public fun TestHostNavigator.testIn(
    scope: CoroutineScope,
    timeout: Duration? = null,
    name: String? = null,
): HostNavigatorTurbine {
    val turbine = events.testIn(scope, timeout, name)
    return DefaultNavigatorTurbine(turbine, state, scope)
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
@Deprecated(
    "Use TestHostNavigator.testIn for back stack events and PlatformNavigator.testIn for activity and permission events. " +
        "See the tests in https://freeletics.github.io/khonshu/navigation/migration-destination-navigator2/",
)
@OptIn(InternalNavigationApi::class)
public fun DestinationNavigator.testIn(
    scope: CoroutineScope,
    timeout: Duration? = null,
    name: String? = null,
): NavigatorTurbine {
    val testHostNavigator = hostNavigator as TestHostNavigator
    return merge(testHostNavigator.events, activityTestEvents()).testEventsIn(testHostNavigator, scope, timeout, name)
}

/**
 * Collects activity and permission events from a navigator created by
 * [TestHostNavigator.platformNavigator] and returns a [PlatformNavigatorTurbine] for asserting them.
 * The returned turbine must be explicitly canceled.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout.
 */
public fun PlatformNavigator.testIn(
    scope: CoroutineScope,
    timeout: Duration? = null,
    name: String? = null,
): PlatformNavigatorTurbine {
    return activityTestEvents().testEventsIn(testHostNavigator(), scope, timeout, name)
}

private suspend fun Flow<TestEvent>.testEvents(
    testHostNavigator: TestHostNavigator,
    timeout: Duration?,
    name: String?,
    validate: suspend NavigatorTurbine.() -> Unit,
) {
    test(timeout, name) {
        val turbine = DefaultNavigatorTurbine(this, testHostNavigator.state, this)
        validate(turbine)
    }
}

private fun Flow<TestEvent>.testEventsIn(
    testHostNavigator: TestHostNavigator,
    scope: CoroutineScope,
    timeout: Duration?,
    name: String?,
): NavigatorTurbine {
    val turbine = testIn(scope, timeout, name)
    return DefaultNavigatorTurbine(turbine, testHostNavigator.state, scope)
}

private fun PlatformNavigator.activityTestEvents(): Flow<TestEvent> {
    return activityEvents.map(::toTestEvent)
}

private fun PlatformNavigator.testHostNavigator(): TestHostNavigator {
    require(this is TestPlatformNavigator) {
        "PlatformNavigator.test/testIn requires a navigator created by TestHostNavigator.platformNavigator()."
    }
    return testHostNavigator
}

/**
 * Asserts back stack navigation events and navigation results. Returned by [TestHostNavigator.test]
 * and [TestHostNavigator.testIn].
 */
public interface HostNavigatorTurbine {
    /**
     * Assert that the next event received was a navigation event to the given [route]. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateTo(route: NavRoute)

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
     * Assert that the next event received was a navigation result. This function will suspend
     * if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun <O : Any> awaitNavigationResult(
        key: NavigationResultRequest.Key<O>,
        result: O,
        serializer: KSerializer<O>,
    )

    /**
     * Cancel this turbine.
     *
     * @throws AssertionError - if there are any unconsumed events.
     */
    public suspend fun cancel()

    /**
     * Cancel this turbine. The difference to [cancel] is any unconsumed event will be
     * ignored and no error will be thrown.
     */
    public suspend fun cancelAndIgnoreRemainingNavEvents()
}

/**
 * Asserts activity and permission events. Returned by [PlatformNavigator.test] and
 * [PlatformNavigator.testIn].
 */
public interface PlatformNavigatorTurbine {
    /**
     * Assert that the next event received was a navigation event to the given [route]. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateTo(route: ActivityRoute, fallbackRoute: NavRoute? = null)

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
     * Cancel this turbine.
     *
     * @throws AssertionError - if there are any unconsumed events.
     */
    public suspend fun cancel()

    /**
     * Cancel this turbine. The difference to [cancel] is any unconsumed event will be
     * ignored and no error will be thrown.
     */
    public suspend fun cancelAndIgnoreRemainingNavEvents()
}

/**
 * Asserts back stack and platform events together. Returned by [DestinationNavigator.test] and
 * [DestinationNavigator.testIn].
 */
@Deprecated("Use HostNavigatorTurbine and PlatformNavigatorTurbine instead.")
public interface NavigatorTurbine : HostNavigatorTurbine, PlatformNavigatorTurbine

public suspend inline fun <reified O : Any> HostNavigatorTurbine.awaitNavigationResult(
    key: NavigationResultRequest.Key<O>,
    result: O,
) {
    awaitNavigationResult(key, result, serializer())
}

internal class DefaultNavigatorTurbine @OptIn(InternalNavigationApi::class) constructor(
    private val turbine: ReceiveTurbine<TestEvent>,
    private val savedStateHandle: StackEntryState,
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

    @OptIn(InternalNavigationApi::class)
    override suspend fun <O : Any> awaitNavigationResult(
        key: NavigationResultRequest.Key<O>,
        result: O,
        serializer: KSerializer<O>,
    ) {
        val turbine = savedStateHandle.getStateFlow<NavigationResult<O>>(
            key = key.requestKey,
            initialValue = NavigationResult(null),
            strategy = NavigationResult.serializer(serializer),
        )
            .map { it.value }
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
