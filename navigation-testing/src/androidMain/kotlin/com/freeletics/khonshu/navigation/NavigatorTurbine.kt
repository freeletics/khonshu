package com.freeletics.khonshu.navigation

import android.os.Parcelable
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import app.cash.turbine.testIn
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.google.common.truth.Truth
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope

/**
 * Collects events from [NavEventNavigator] and and allows the [validate] lambda to consume
 * and assert properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public suspend fun NavEventNavigator.test(
    timeout: Duration? = null,
    name: String? = null,
    validate: suspend NavigatorTurbine.() -> Unit,
) {
    val navigator = this
    navEvents.test(timeout, name) {
        val turbine = DefaultNavigatorTurbine(navigator, this)
        validate(turbine)
    }
}

/**
 * Collects events from [NavEventNavigator] and returns a [NavigatorTurbine] for consuming
 * and asserting properties on them in order. If any exception occurs during validation the
 * exception is rethrown from this method.
 *
 * Unlike test which automatically cancels the flow at the end of the lambda, the returned
 * NavigatorTurbine be explicitly canceled.
 *
 * [timeout] - If non-null, overrides the current Turbine timeout inside validate.
 */
public fun NavEventNavigator.testIn(
    scope: CoroutineScope,
    timeout: Duration? = null,
    name: String? = null,
): NavigatorTurbine {
    val turbine = navEvents.testIn(scope, timeout, name)
    return DefaultNavigatorTurbine(this, turbine)
}

/**
 * Causes an emission to the current [NavEventNavigator.backPresses] collector to make it possible
 * to simulate a back press in tests that check custom back press logic.
 */
public fun NavEventNavigator.dispatchBackPress() {
    onBackPressedCallback.handleOnBackPressed()
}

public interface NavigatorTurbine {

    /**
     * Causes an emission to the current [NavEventNavigator.backPresses] collector to make it possible
     * to simulate a back press in tests that check custom back press logic.
     */
    public fun dispatchBackPress()

    /**
     * Assert that the next event received was a navigation event to the given [route]. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateTo(route: NavRoute)

    /**
     * Assert that the next event received was a navigation event to the given [root]. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateToRoot(
        root: NavRoot,
        restoreRootState: Boolean,
    )

    /**
     * Assert that the next event received was a navigation event to the given [route]. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitNavigateTo(route: ActivityRoute)

    /**
     * Assert that all the events in the [block] are received. This function
     * will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event is not a MultiNavEvent followed by the events contained in the block
     */
    public suspend fun awaitNavigate(block: TestNavEventCollector.() -> Unit)

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
     * Assert that the next event received was a "reset to root" navigation event with matching
     * parameters. This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitResetToRoot(
        root: NavRoot,
    )

    /**
     * Assert that the next event received was a "replace all" navigation event with matching
     * parameters. This function will suspend if no events have been received.
     *
     * @throws AssertionError - if the next event was not a matching event.
     */
    public suspend fun awaitReplaceAll(
        root: NavRoot,
    )

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
    private val navigator: NavEventNavigator,
    private val turbine: ReceiveTurbine<NavEvent>,
) : NavigatorTurbine {

    override fun dispatchBackPress() {
        navigator.onBackPressedCallback.handleOnBackPressed()
    }

    override suspend fun awaitNavigateTo(route: NavRoute) {
        val event = NavEvent.NavigateToEvent(route)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateToRoot(
        root: NavRoot,
        restoreRootState: Boolean,
    ) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateTo(route: ActivityRoute) {
        val event = NavEvent.NavigateToActivityEvent(route)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigate(block: TestNavEventCollector.() -> Unit) {
        val navEvents = TestNavEventCollector().apply(block).navEvents
        val multiNavEvent = NavEvent.MultiNavEvent(navEvents)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(multiNavEvent)
    }

    override suspend fun awaitNavigateUp() {
        val event = NavEvent.UpEvent
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateBack() {
        val event = NavEvent.BackEvent
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun <T : NavRoute> awaitNavigateBackTo(
        popUpTo: KClass<T>,
        inclusive: Boolean,
    ) {
        val event = NavEvent.BackToEvent(DestinationId(popUpTo), inclusive)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitResetToRoot(
        root: NavRoot,
    ) {
        val event = NavEvent.ResetToRoot(root)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitReplaceAll(root: NavRoot) {
        val event = NavEvent.ReplaceAll(root)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun awaitNavigateForResult(request: ActivityResultRequest<Void?, *>) {
        awaitNavigateForResult(request, null)
    }

    override suspend fun <I> awaitNavigateForResult(
        request: ActivityResultRequest<I, *>,
        input: I,
    ) {
        val event = NavEvent.ActivityResultEvent(request, input)
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
        val event = NavEvent.ActivityResultEvent(request, permissions)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun <O : Parcelable> awaitNavigationResult(
        key: NavigationResultRequest.Key<O>,
        result: O,
    ) {
        val event = NavEvent.DestinationResultEvent(key, result)
        Truth.assertThat(turbine.awaitItem()).isEqualTo(event)
    }

    override suspend fun cancel() {
        turbine.cancel()
    }

    override suspend fun cancelAndIgnoreRemainingNavEvents() {
        turbine.cancelAndIgnoreRemainingEvents()
    }
}
