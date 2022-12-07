package com.freeletics.mad.navigator.internal

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.NavigationResultRequest
import kotlin.reflect.KClass

@InternalNavigatorApi
public class AndroidXNavigationExecutor(
    private val controller: NavController
) : NavigationExecutor {

    override fun navigate(route: NavRoute) {
        controller.navigate(route.destinationId(), route.getArguments())
    }

    override fun navigate(route: ActivityRoute) {
        controller.navigate(route.destinationId(), route.getArguments())
    }

    override fun navigate(root: NavRoot, restoreRootState: Boolean) {
        val options = NavOptions.Builder()
            // save the state of the current root before leaving it
            .setPopUpTo(controller.graph.startDestinationId, inclusive = false, saveState = true)
            // restoring the state of the target root
            .setRestoreState(restoreRootState)
            // makes sure that if the destination is already on the backstack, it and
            // everything above it gets removed
            .setLaunchSingleTop(true)
            .build()
        controller.navigate(root.destinationId(), root.getArguments(), options)
    }

    override fun navigateBack() {
        controller.popBackStack()
    }

    override fun navigateUp() {
        controller.navigateUp()
    }

    override fun deliverResult(key: NavigationResultRequest.Key<*>, result: Parcelable) {
        savedStateHandleFor(key.route)[key.requestKey] = result
    }

    override fun navigateBackTo(route: KClass<out BaseRoute>, isInclusive: Boolean) {
        controller.popBackStack(route.destinationId(), isInclusive)
    }

    override fun savedStateHandleFor(route: KClass<out BaseRoute>): SavedStateHandle {
        return entryFor(route).savedStateHandle
    }

    override fun <T : BaseRoute> routeFor(route: KClass<T>): T {
        return entryFor(route).arguments.requireRoute()
    }

    override fun viewModelStoreFor(route: KClass<out BaseRoute>): ViewModelStore {
        return entryFor(route).viewModelStore
    }

    private fun entryFor(route: KClass<out BaseRoute>): NavBackStackEntry {
        return controller.getBackStackEntry(route.destinationId())
    }
}
