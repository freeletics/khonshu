package com.freeletics.khonshu.navigation.activity

import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * This allows to trigger [androidx.activity.result.contract.ActivityResultContract] related
 * navigation actions from outside the view layer without keeping references to Android framework
 * classes that might leak.
 *
 * It also improves the testability of your navigation logic since it is possible to just write
 * test that the correct events were emitted.
 *
 * For this work [ActivityNavigatorEffect] needs to be called.
 */
public abstract class ActivityNavigator {
    private val _activityEvents = Channel<ActivityEvent>(Channel.Factory.UNLIMITED)

    @InternalNavigationTestingApi
    public val activityEvents: Flow<ActivityEvent> = _activityEvents.receiveAsFlow()

    private val _activityResultRequests = mutableListOf<ActivityResultContractRequest<*, *, *>>()
    private var allowedToAddRequests = true

    @InternalNavigationTestingApi
    public val activityResultRequests: List<ActivityResultContractRequest<*, *, *>>
        get() {
            allowedToAddRequests = false
            return _activityResultRequests.toList()
        }

    /**
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: ActivityRoute, fallbackRoute: NavRoute? = null) {
        val event = ActivityEvent.NavigateTo(route, fallbackRoute)
        sendEvent(event)
    }

    /**
     * Register for receiving activity results for the given [contract].
     *
     * The returned [com.freeletics.khonshu.navigation.ActivityResultRequest] can be used to collect incoming results (via
     * [com.freeletics.khonshu.navigation.ActivityResultRequest.results]) and to launch the actual activity result call via
     * [navigateForResult].
     *
     * For permission requests prefer using [registerForPermissionsResult] instead.
     *
<<<<<<< HEAD
     * Note: You must call this before [com.freeletics.khonshu.navigation.NavigationSetup] is called with
     * this navigator.
=======
     * Note: You must call this before [ActivityNavigatorEffect] is called with this navigator.
>>>>>>> 0a0d838b (move Android APIs to extra package)
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
     * The returned [com.freeletics.khonshu.navigation.PermissionsResultRequest] can be used to collect incoming permission results (via
     * [com.freeletics.khonshu.navigation.PermissionsResultRequest.results]) and to launch the actual permission result call via
     * [requestPermissions].
     *
     * Compared to using [registerForActivityResult] with
     * [androidx.activity.result.contract.ActivityResultContracts.RequestPermission] this provides
     * a `PermissionResult` instead of a `boolean. See `[com.freeletics.khonshu.navigation.PermissionsResultRequest.PermissionResult]`
     * for more information.
     *
<<<<<<< HEAD
     * Note: You must call this before [com.freeletics.khonshu.navigation.NavigationSetup] is called with
     * this navigator.
=======
     * Note: You must call this before [ActivityNavigatorEffect] is called with this navigator.
>>>>>>> 0a0d838b (move Android APIs to extra package)
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
        val event = ActivityEvent.NavigateForResult(request, input)
        sendEvent(event)
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
        val event = ActivityEvent.NavigateForResult(request, permissions)
        sendEvent(event)
    }

    private fun sendEvent(event: ActivityEvent) {
        val result = _activityEvents.trySendBlocking(event)
        check(result.isSuccess)
    }

    internal fun checkAllowedToAddRequests() {
        check(allowedToAddRequests) {
            "Failed to register for result! You must call this before NavigationSetup is called with this navigator."
        }
    }
}
