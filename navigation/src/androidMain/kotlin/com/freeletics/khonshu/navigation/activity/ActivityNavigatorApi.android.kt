package com.freeletics.khonshu.navigation.activity

import androidx.activity.result.contract.ActivityResultContract
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.activity.internal.ActivityEvent
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import kotlinx.coroutines.flow.Flow

public actual interface ActivityNavigatorApi {
    @InternalNavigationTestingApi
    public val activityEvents: Flow<ActivityEvent>

    @InternalNavigationTestingApi
    public val activityResultRequests: List<ActivityResultContractRequest<*, *, *>>

    /**
     * Triggers navigation to the given [route].
     */
    public fun navigateTo(route: ActivityRoute, fallbackRoute: NavRoute? = null)

    /**
     * Register for receiving activity results for the given [contract].
     */
    public fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
    ): ActivityResultRequest<I, O>

    /**
     * Register for receiving permission results.
     */
    public fun registerForPermissionsResult(): PermissionsResultRequest

    /**
     * Launches the given [request].
     */
    public fun navigateForResult(request: ActivityResultRequest<Void?, *>)

    /**
     * Launches the given [request] with the given [input].
     */
    public fun <I> navigateForResult(request: ActivityResultRequest<I, *>, input: I)

    /**
     * Launches the [request] for the given [permissions].
     */
    public fun requestPermissions(request: PermissionsResultRequest, vararg permissions: String)

    /**
     * Launches the [request] for the given [permissions].
     */
    public fun requestPermissions(request: PermissionsResultRequest, permissions: List<String>)
}
