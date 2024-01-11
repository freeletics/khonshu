package com.freeletics.khonshu.navigation.androidx.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import java.io.Serializable

@InternalNavigationApi
public class AndroidXNavigationExecutor(
    private val controller: NavController,
    private val onSaveStartRoute: (NavRoot) -> Unit,
) : NavigationExecutor {

    override fun navigateTo(route: NavRoute) {
        controller.navigate(route.destinationId(), route.getArguments())
    }

    override fun navigateTo(route: ActivityRoute) {
        controller.navigate(route.destinationId(), route.getArguments())
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
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

    override fun <T : BaseRoute> navigateBackToInternal(popUpTo: DestinationId<T>, inclusive: Boolean) {
        controller.popBackStack(popUpTo.destinationId(), inclusive)
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

    override fun replaceAll(root: NavRoot) {
        val options = navOptions {
            // pop all entries from the backstack
            popUpTo(id = controller.graph.id) {
                inclusive = true
                saveState = false
            }

            // Avoid multiple copies of the same destination.
            launchSingleTop = true

            // Don't restore the state of the target destination.
            restoreState = false
        }

        controller.navigate(root.destinationId(), root.getArguments(), options)
        onSaveStartRoute(root)
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

    override fun <T : BaseRoute> extra(destinationId: DestinationId<T>): Serializable {
        val destination = entryFor(destinationId).destination
        val extraArgument = destination.arguments["NAV_SECRET_EXTRA"]!!
        return extraArgument.defaultValue as Serializable
    }

    private fun entryFor(destinationId: DestinationId<*>): NavBackStackEntry {
        return controller.getBackStackEntry(destinationId.destinationId())
    }
}
