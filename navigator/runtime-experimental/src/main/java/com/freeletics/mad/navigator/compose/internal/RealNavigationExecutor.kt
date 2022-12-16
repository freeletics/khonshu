package com.freeletics.mad.navigator.compose.internal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.compose.NavDestination
import com.freeletics.mad.navigator.internal.DestinationId
import com.freeletics.mad.navigator.internal.NavigationExecutor

@Composable
internal fun rememberNavigationExecutor(
    startRoot: BaseRoute,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
) : RealNavigationExecutor {
    val context = LocalContext.current
    return remember {
        RealNavigationExecutor(context, startRoot, destinations, deepLinkHandlers ,deepLinkPrefixes)
    }
}

internal class RealNavigationExecutor(
    private val context: Context,
    startRoot: BaseRoute,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
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
