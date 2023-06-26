package com.freeletics.mad.navigation.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.freeletics.mad.navigation.ActivityRoute
import com.freeletics.mad.navigation.BaseRoute
import com.freeletics.mad.navigation.NavRoot
import com.freeletics.mad.navigation.NavRoute

@InternalNavigationApi
public class AndroidXNavigationExecutor(
    private val controller: NavController,
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
            .setPopUpTo(
                controller.graph.startDestinationId,
                inclusive = false,
                saveState = true,
            )
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

    override fun <T : BaseRoute> navigateBackTo(destinationId: DestinationId<T>, isInclusive: Boolean) {
        controller.popBackStack(destinationId.destinationId(), isInclusive)
    }

    override fun resetToRoot(root: NavRoot) {
        val options = NavOptions.Builder()
            // save the state of the current root before leaving it
            .setPopUpTo(
                controller.graph.startDestinationId,
                inclusive = false,
                saveState = false,
            )
            // restoring the state of the target root
            .setRestoreState(false)
            // makes sure that if the destination is already on the backstack, it and
            // everything above it gets removed
            .setLaunchSingleTop(true)
            .build()
        controller.navigate(root.destinationId(), root.getArguments(), options)
    }

    override fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle {
        return entryFor(destinationId).savedStateHandle
    }

    override fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T {
        return entryFor(destinationId).arguments.requireRoute()
    }

    override fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): NavigationExecutor.Store {
        val viewModelStore = entryFor(destinationId).viewModelStore
        val factory = ViewModelProvider.NewInstanceFactory()
        return ViewModelProvider(viewModelStore, factory)[StoreViewModel::class.java]
    }

    private fun entryFor(destinationId: DestinationId<*>): NavBackStackEntry {
        return controller.getBackStackEntry(destinationId.destinationId())
    }
}
