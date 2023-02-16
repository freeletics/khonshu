package com.freeletics.mad.navigator.compose.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.compose.ActivityDestination
import com.freeletics.mad.navigator.compose.ContentDestination
import com.freeletics.mad.navigator.internal.DestinationId
import com.freeletics.mad.navigator.internal.NavigationExecutor

internal class MultiStackNavigationExecutor(
    @Suppress("unused") //TODO
    private val activityStarter: (ActivityRoute, ActivityDestination) -> Unit,
    @Suppress("unused") //TODO
    private val contentDestinations: List<ContentDestination<*>>,
    @Suppress("unused") //TODO
    private val activityDestinations: List<ActivityDestination>,
) : NavigationExecutor {

    override fun navigate(route: NavRoute) {
        TODO("Not yet implemented")
    }

    override fun navigate(root: NavRoot, restoreRootState: Boolean, saveCurrentRootState: Boolean) {
        TODO("Not yet implemented")
    }

    override fun navigate(route: ActivityRoute) {
        TODO("Not yet implemented")
    }

    override fun navigateUp() {
        TODO("Not yet implemented")
    }

    override fun navigateBack() {
        TODO("Not yet implemented")
    }

    override fun <T : BaseRoute> navigateBackTo(
        destinationId: DestinationId<T>,
        isInclusive: Boolean,
    ) {
        TODO("Not yet implemented")
    }

    override fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle {
        TODO("Not yet implemented")
    }

    override fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): NavigationExecutor.Store {
        TODO("Not yet implemented")
    }
}
