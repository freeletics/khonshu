package com.freeletics.mad.navigator.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
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

    override fun navigate(root: NavRoot, restoreRootState: Boolean, saveCurrentRootState: Boolean) {
        val options = NavOptions.Builder()
            // save the state of the current root before leaving it
            .setPopUpTo(
                controller.graph.startDestinationId,
                inclusive = false,
                saveState = saveCurrentRootState
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

    override fun navigateBackTo(destinationId: DestinationId<*>, isInclusive: Boolean) {
        controller.popBackStack(destinationId.destinationId(), isInclusive)
    }

    override fun savedStateHandleFor(destinationId: DestinationId<*>): SavedStateHandle {
        return entryFor(destinationId).savedStateHandle
    }

    override fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T {
        return entryFor(destinationId).arguments.requireRoute()
    }

    override fun storeFor(destinationId: DestinationId<*>): NavigationExecutor.Store {
        return ViewModelProvider(entryFor(destinationId).viewModelStore, ViewModelProvider.NewInstanceFactory())[StoreViewModel::class.java]
    }

    private fun entryFor(destinationId: DestinationId<*>): NavBackStackEntry {
        return controller.getBackStackEntry(destinationId.destinationId())
    }
}
