package com.freeletics.khonshu.navigation

import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.NavEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * This allows to trigger [ActivityResultContract] related navigation actions from outside t
 * he view layer without keeping references to Android framework classes that might leak.
 * It also improves the testability of your navigation logic since it is possible to just write
 * test that the correct events were emitted.
 *
 * For this work [NavigationSetup] needs to be called.
 */
public abstract class ActivityNavigator {

    private val _navEvents = Channel<NavEvent>(Channel.UNLIMITED)

    @InternalNavigationTestingApi
    public val navEvents: Flow<NavEvent> = _navEvents.receiveAsFlow()

    private val _activityResultRequests = mutableListOf<ContractResultOwner<*, *, *>>()
    internal var allowedToAddRequests = true

    @InternalNavigationTestingApi
    public val activityResultRequests: List<ContractResultOwner<*, *, *>>
        get() {
            allowedToAddRequests = false
            return _activityResultRequests.toList()
        }

    /**
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: ActivityRoute, fallbackRoute: NavRoute? = null) {
        val event = NavEvent.NavigateToActivityEvent(route, fallbackRoute)
        sendNavEvent(event)
    }

    /**
     * Register for receiving activity results for the given [contract].
     *
     * The returned [ActivityResultRequest] can be used to collect incoming results (via
     * [ActivityResultRequest.results]) and to launch the actual activity result call via
     * [navigateForResult].
     *
     * For permission requests prefer using [registerForPermissionsResult] instead.
     *
     * Note: You must call this before [NavigationSetup] is called with this navigator.
     */
    public fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
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
     * [requestPermissions].
     *
     * Compared to using [registerForActivityResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     *
     * Note: You must call this before [NavigationSetup] is called with this navigator.
     */
    public fun registerForPermissionsResult(): PermissionsResultRequest {
        checkAllowedToAddRequests()
        val request = PermissionsResultRequest()
        _activityResultRequests.add(request)
        return request
    }

    /**
     * Launches the given [request].
     */
    public fun navigateForResult(request: ActivityResultRequest<Void?, *>) {
        navigateForResult(request, null)
    }

    /**
     * Launches the given [request] with the given [input].
     */
    public fun <I> navigateForResult(request: ActivityResultRequest<I, *>, input: I) {
        val event = NavEvent.ActivityResultEvent(request, input)
        sendNavEvent(event)
    }

    /**
     * Launches the [request] for the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     */
    public fun requestPermissions(request: PermissionsResultRequest, vararg permissions: String) {
        requestPermissions(request, permissions.toList())
    }

    /**
     * Launches the [request] for the given [permissions].
     *
     * Compared to using [navigateForResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[PermissionsResultRequest.PermissionResult]`
     * for more information.
     */
    public fun requestPermissions(request: PermissionsResultRequest, permissions: List<String>) {
        val event = NavEvent.ActivityResultEvent(request, permissions)
        sendNavEvent(event)
    }

    internal fun sendNavEvent(event: NavEvent) {
        val result = _navEvents.trySendBlocking(event)
        check(result.isSuccess)
    }

    internal fun checkAllowedToAddRequests() {
        check(allowedToAddRequests) {
            "Failed to register for result! You must call this before NavigationSetup is called with this navigator."
        }
    }
}
