package com.freeletics.mad.navigator

import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.freeletics.mad.navigator.NavEvent.ActivityResultEvent
import com.freeletics.mad.navigator.NavEvent.BackEvent
import com.freeletics.mad.navigator.NavEvent.BackToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateBackAndThenToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToEvent
import com.freeletics.mad.navigator.NavEvent.PermissionsResultEvent
import com.freeletics.mad.navigator.NavEvent.UpEvent
import com.freeletics.mad.navigator.internal.DelegatingOnBackPressedCallback
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A [Navigator] implementation that allows to fire [events][NavEvent] through it's methods. These
 * events are publicly exposed through the [navEvents] [Flow] that can be collected and acted upon
 * by a `NavigationHandler`. This allows to trigger navigation actions from outside the view layer
 * without keeping references to Android framework classes that might leak. It also improves
 * the testability of your navigation logic since it is possible to just write test that
 * the correct events were emitted.
 *
 * For back press handling based on logic [backPresses] is available. Activity results and
 * permission requests can be handled through [registerForActivityResult]/[navigateForResult]
 * and [registerForPermissionsResult]/[requestPermissions] respectively.
 */
public abstract class NavEventNavigator {

    private val _navEvents = Channel<NavEvent>(Channel.UNLIMITED)

    /**
     * A [Flow] to collect [NavEvents][NavEvent] produced by this navigator.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public val navEvents: Flow<NavEvent> = _navEvents.receiveAsFlow()

    private val _activityResultRequests = mutableListOf<ActivityResultRequest<*, *>>()
    private val _permissionsResultRequests = mutableListOf<PermissionsResultRequest>()
    private val _onBackPressedCallback = DelegatingOnBackPressedCallback()

    /**
     * Register for receiving activity results for the given [contract].
     *
     * The returned [ActivityResultRequest] can be used to collect incoming results (via
     * [ActivityResultRequest.results]) and to launch the actual activity result call via
     * [NavEventNavigator.navigateForResult].
     *
     * For permission requests prefer using [registerForPermissionsResult] instead.
     *
     * Note: This has to be called *before* this [NavEventNavigator] gets attached to a fragment.
     *   In practice, this means it should usually be called during initialisation of your subclass.
     */
    public fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>
    ): ActivityResultRequest<I, O> {
        checkAllowedToAddRequests()
        val request = ActivityResultRequest(contract)
        _activityResultRequests.add(request)
        return request
    }

    /**
     * Register for receiving permission results.
     *
     * The returned [PermissionsResultRequest] can be used to collect incoming permission results (via
     * [PermissionsResultRequest.results]) and to launch the actual permission result call via
     * [NavEventNavigator.requestPermissions].
     *
     * Compared to using [registerForActivityResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this also
     * gives you the information whether a permission was
     * [permanently denied][PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY].
     *
     * Note: This has to be called *before* this [NavEventNavigator] gets attached to a fragment.
     *   In practice, this means it should usually be called during initialisation of your subclass.
     */
    public fun registerForPermissionsResult(): PermissionsResultRequest {
        checkAllowedToAddRequests()
        val request = PermissionsResultRequest()
        _permissionsResultRequests.add(request)
        return request
    }

    /**
     * Triggers a new [NavEvent] to navigate to the given [route].
     */
    public fun navigateTo(route: NavRoute) {
        val event = NavigateToEvent(route)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that pops the back stack to [popUpTo]. If [inclusive] is `true`
     * [popUpTo] destination itself will also be popped. Afterwards it will navigate to [route].
     */
    public fun navigateTo(
        route: NavRoute,
        @IdRes popUpTo: Int,
        inclusive: Boolean = false
    ) {
        val event = NavigateBackAndThenToEvent(route, popUpTo, inclusive)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] to navigate to the given [root]. The current back stack will
     * be popped and saved. Whether the backstack of the given root is restored depends
     * on [restoreRootState].
     */
    public fun navigateToRoot(
        root: NavRoot,
        restoreRootState: Boolean = false,
    ) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that causes up navigation.
     */
    public fun navigateUp() {
        val event = UpEvent
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that pops the back stack to the previous destination.
     */
    public fun navigateBack() {
        val event = BackEvent
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that pops the back stack to [destinationId]. If [inclusive] is
     * `true` [destinationId] itself will also be popped.
     */
    public fun navigateBack(@IdRes destinationId: Int, inclusive: Boolean = false) {
        val event = BackToEvent(destinationId, inclusive)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that launches the given [launcher].
     *
     * The [launcher] can be obtained by calling [registerForActivityResult].
     */
    public fun navigateForResult(launcher: ActivityResultRequest<Void?, *>) {
        navigateForResult(launcher, null)
    }

    /**
     * Triggers a new [NavEvent] that launches the given [launcher] with the given [input].
     *
     * The [launcher] can be obtained by calling [registerForActivityResult].
     */
    public fun <I> navigateForResult(launcher: ActivityResultRequest<I, *>, input: I) {
        val event = ActivityResultEvent(launcher, input)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that requests the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this also
     * gives you the information whether a permission was
     * [permanently denied][PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY].
     *
     * The [request] can be obtained by calling [registerForPermissionsResult].
     */
    public fun requestPermissions(request: PermissionsResultRequest, vararg permissions: String) {
        requestPermissions(request, permissions.toList())
    }

    /**
     * Triggers a new [NavEvent] that requests the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this also
     * gives you the information whether a permission was
     * [permanently denied][PermissionsResultRequest.PermissionResult.DENIED_PERMANENTLY].
     *
     * The [request] can be obtained by calling [registerForPermissionsResult].
     */
    public fun requestPermissions(request: PermissionsResultRequest, permissions: List<String>) {
        val event = PermissionsResultEvent(request, permissions)
        sendNavEvent(event)
    }

    private fun sendNavEvent(event: NavEvent) {
        val result = _navEvents.trySendBlocking(event)
        check(result.isSuccess)
    }

    /**
     * Returns a [Flow] that will emit [Unit] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    @ExperimentalCoroutinesApi
    public fun backPresses(): Flow<Unit> {
        return backPresses(Unit)
    }

    /**
     * Returns a [Flow] that will emit [value] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    @ExperimentalCoroutinesApi
    public fun <T> backPresses(value: T): Flow<T> {
        return callbackFlow {
            val onBackPressed = {
                check(trySendBlocking(value).isSuccess)
            }

            _onBackPressedCallback.addCallback(onBackPressed)

            awaitClose {
                _onBackPressedCallback.removeCallback(onBackPressed)
            }
        }
    }

    private var allowedToAddRequests = true

    private fun checkAllowedToAddRequests() {
        check(allowedToAddRequests) {
            "Failed to register for result! You must call this before this navigator " +
                "gets attached to a fragment, e.g. during initialisation of your navigator subclass."
        }
    }

    /**
     * Access to [ActivityResultRequest] objects that were registered with
     * [registerForActivityResult]. A `NavEventNavigationHandler` can use these to register
     * the requests during setup so that results are delivered to them.
     */
    @InternalNavigatorApi
    public val activityResultRequests: List<ActivityResultRequest<*, *>>
        get() {
            allowedToAddRequests = false
            return _activityResultRequests.toList()
        }
    /**
     * Access to [PermissionsResultRequest] objects that were registered with
     * [registerForPermissionsResult]. A `NavEventNavigationHandler` can use these to register
     * the requests during setup so that results are delivered to them.
     */
    @InternalNavigatorApi
    public val permissionsResultRequests: List<PermissionsResultRequest>
        get() {
            allowedToAddRequests = false
            return _permissionsResultRequests.toList()
        }

    /**
     * Access to the internal [OnBackPressedCallback] that backs the [backPresses] `Flow`.
     * A `NavEventNavigationHandler` can add this to an `OnBackPressedDispatcher` to enable
     * [backPresses].
     */
    @InternalNavigatorApi
    public val onBackPressedCallback: OnBackPressedCallback get() = _onBackPressedCallback
}
