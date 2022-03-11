package com.freeletics.mad.navigator

import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import com.freeletics.mad.navigator.NavEvent.ActivityResultEvent
import com.freeletics.mad.navigator.NavEvent.BackEvent
import com.freeletics.mad.navigator.NavEvent.BackToEvent
import com.freeletics.mad.navigator.NavEvent.NavigateToEvent
import com.freeletics.mad.navigator.NavEvent.PermissionsResultEvent
import com.freeletics.mad.navigator.NavEvent.UpEvent
import com.freeletics.mad.navigator.internal.DelegatingOnBackPressedCallback
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.destinationId
import kotlin.reflect.KClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A navigator that allows to fire [events][NavEvent] through it's methods. These
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
public open class NavEventNavigator {

    private val _navEvents = Channel<NavEvent>(Channel.UNLIMITED)

    /**
     * A [Flow] to collect [NavEvents][NavEvent] produced by this navigator.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public val navEvents: Flow<NavEvent> = _navEvents.receiveAsFlow()

    private val _activityResultRequests = mutableListOf<ActivityResultRequest<*, *>>()
    private val _permissionsResultRequests = mutableListOf<PermissionsResultRequest>()
    private val _navigationResultRequests = mutableListOf<NavigationResultRequest<*>>()
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
     * Register for receiving navigation results that were delivered through
     * [deliverNavigationResult]. [T] is expected to be the [BaseRoute] to the current destination.
     *
     * The returned [NavigationResultRequest] has a [NavigationResultRequest.Key]. This `key` should
     * be passed to the target destination which can then use it to call [deliverNavigationResult].
     *
     * Note: This has to be called *before* this [NavEventNavigator] gets attached to a fragment.
     *   In practice, this means it should usually be called during initialisation of your subclass.
     */
    public inline fun <reified T : BaseRoute, reified O : Parcelable> registerForNavigationResult():
        NavigationResultRequest<O> {
        return registerForNavigationResult(T::class, O::class)
    }

    @InternalNavigatorApi
    public fun <O : Parcelable> registerForNavigationResult(
        route: KClass<out BaseRoute>,
        result: KClass<*>
    ): NavigationResultRequest<O> {
        checkAllowedToAddRequests()
        val requestKey = route.qualifiedName!! + result.qualifiedName!!
        val key = NavigationResultRequest.Key(route.destinationId(), requestKey)
        val request = NavigationResultRequest<O>(key)
        _navigationResultRequests.add(request)
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
     * Triggers a new [NavEvent] that pops the back stack to [T]. If [inclusive] is
     * `true` [T] itself will also be popped.
     */
    public inline fun <reified T: BaseRoute> navigateBackTo(inclusive: Boolean = false) {
        navigateBackTo(T::class, inclusive)
    }

    @InternalNavigatorApi
    public fun navigateBackTo(popUpTo: KClass<out BaseRoute>, inclusive: Boolean = false) {
        val event = BackToEvent(popUpTo, inclusive)
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

    /**
     * Delivers the [result] to the destination that created [key].
     */
    public fun deliverNavigationResult(key: NavigationResultRequest.Key, result: Parcelable) {
        val event = NavEvent.DestinationResultEvent(key, result)
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

    @InternalNavigatorApi
    public val activityResultRequests: List<ActivityResultRequest<*, *>>
        get() {
            allowedToAddRequests = false
            return _activityResultRequests.toList()
        }

    @InternalNavigatorApi
    public val permissionsResultRequests: List<PermissionsResultRequest>
        get() {
            allowedToAddRequests = false
            return _permissionsResultRequests.toList()
        }

    @InternalNavigatorApi
    public val navigationResultRequest: List<NavigationResultRequest<*>>
        get() {
            allowedToAddRequests = false
            return _navigationResultRequests.toList()
        }

    @InternalNavigatorApi
    public val onBackPressedCallback: OnBackPressedCallback get() = _onBackPressedCallback
}
