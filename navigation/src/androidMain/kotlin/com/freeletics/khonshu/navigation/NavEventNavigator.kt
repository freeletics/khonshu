package com.freeletics.khonshu.navigation

import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import com.freeletics.khonshu.navigation.internal.DelegatingOnBackPressedCallback
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.NavEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.BackEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.BackToEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.MultiNavEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.NavigateToEvent
import com.freeletics.khonshu.navigation.internal.NavEvent.UpEvent
import com.freeletics.khonshu.navigation.internal.NavEventCollector
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * This allows to trigger navigation actions from outside the view layer
 * without keeping references to Android framework classes that might leak. It also improves
 * the testability of your navigation logic since it is possible to just write test that
 * the correct events were emitted.
 *
 * For back press handling based on logic [backPresses] is available. Activity results and
 * permission requests can be handled through [registerForActivityResult]/[navigateForResult]
 * and [registerForPermissionsResult]/[requestPermissions] respectively.
 */
public open class NavEventNavigator : Navigator, ResultNavigator, ActivityNavigator(), BackInterceptor {
    private val _navigationResultRequests = mutableListOf<EventNavigationResultRequest<*>>()
    private val _onBackPressedCallback = DelegatingOnBackPressedCallback()

    @InternalNavigationTestingApi
    override fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        checkAllowedToAddRequests()
        val requestKey = "${id.route.qualifiedName!!}-$resultType"
        val key = NavigationResultRequest.Key<O>(id, requestKey)
        val request = EventNavigationResultRequest(key)
        _navigationResultRequests.add(request)
        return request
    }

    /**
     * Triggers a new [NavEvent] to navigate to the given [route].
     */
    override fun navigateTo(route: NavRoute) {
        val event = NavigateToEvent(route)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] to navigate to the given [root]. The current back stack will
     * be popped and saved. Whether the backstack of the given `root` is restored depends on
     * [restoreRootState].
     */
    override fun navigateToRoot(
        root: NavRoot,
        restoreRootState: Boolean,
    ) {
        val event = NavEvent.NavigateToRootEvent(root, restoreRootState)
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that causes up navigation.
     */
    override fun navigateUp() {
        val event = UpEvent
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that pops the back stack to the previous destination.
     */
    override fun navigateBack() {
        val event = BackEvent
        sendNavEvent(event)
    }

    /**
     * Triggers a new [NavEvent] that collects and combines the nav events sent in the block so they can be
     * handled individually.
     *
     * Note: This should be used when navigating multiple times, for example calling `navigateBackTo`
     * followed by `navigateTo`.
     */
    public fun navigate(block: Navigator.() -> Unit) {
        val navEvents = NavEventCollector().apply(block).navEvents
        sendNavEvent(MultiNavEvent(navEvents))
    }

    override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
        val event = BackToEvent(popUpTo, inclusive)
        sendNavEvent(event)
    }

    /**
     * Reset the back stack to the given [root]. The current back stack will cleared and if
     * root was already on it it will be recreated.
     */
    override fun resetToRoot(root: NavRoot) {
        val event = NavEvent.ResetToRoot(root)
        sendNavEvent(event)
    }

    public override fun replaceAll(root: NavRoot) {
        val event = NavEvent.ReplaceAll(root)
        sendNavEvent(event)
    }

    /**
     * Delivers the [result] to the destination that created [key].
     */
    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        val event = NavEvent.DestinationResultEvent(key, result)
        sendNavEvent(event)
    }

    /**
     * Returns a [Flow] that will emit [value] on every back press. While this Flow is being collected
     * all back presses will be intercepted and none of the default back press handling happens.
     *
     * When this is called multiple times only the latest caller will receive emissions.
     */
    override fun <T> backPresses(value: T): Flow<T> {
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

    @InternalNavigationTestingApi
    public val navigationResultRequests: List<EventNavigationResultRequest<*>>
        get() {
            allowedToAddRequests = false
            return _navigationResultRequests.toList()
        }

    @InternalNavigationTestingApi
    public val onBackPressedCallback: OnBackPressedCallback get() = _onBackPressedCallback
}
