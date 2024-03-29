package com.freeletics.khonshu.navigation.internal

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.ActivityDestination
import com.freeletics.khonshu.navigation.ContentDestination
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.EXTRA_DEEPLINK_ROUTES
import com.freeletics.khonshu.navigation.deeplinks.buildIntent
import com.freeletics.khonshu.navigation.deeplinks.createDeepLinkIfMatching
import com.freeletics.khonshu.navigation.findActivity
import com.freeletics.khonshu.navigation.internal.MultiStackNavigationExecutor.Companion.SAVED_STATE_HANDLED_DEEP_LINKS
import com.freeletics.khonshu.navigation.internal.MultiStackNavigationExecutor.Companion.SAVED_STATE_STACK
import kotlinx.collections.immutable.ImmutableSet

@Composable
internal fun rememberNavigationExecutor(
    startRoot: NavRoot,
    destinations: ImmutableSet<NavDestination>,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
): MultiStackNavigationExecutor {
    val context = LocalContext.current

    val viewModel = viewModel<StoreViewModel>(factory = SavedStateViewModelFactory())

    val starter = remember(context, destinations) {
        val activityDestinations = destinations.filterIsInstance<ActivityDestination>()
        ActivityStarter(context, activityDestinations)
    }

    val stack = remember(destinations, viewModel, startRoot) {
        val contentDestinations = destinations.filterIsInstance<ContentDestination<*>>()
        val navState = viewModel.globalSavedStateHandle.get<Bundle>(SAVED_STATE_STACK)

        if (navState == null) {
            MultiStack.createWith(
                root = startRoot,
                destinations = contentDestinations,
                onStackEntryRemoved = viewModel::removeEntry,
            )
        } else {
            MultiStack.fromState(
                root = startRoot,
                bundle = navState,
                destinations = contentDestinations,
                onStackEntryRemoved = viewModel::removeEntry,
            )
        }
    }

    val deepLinkRoutes = remember(viewModel, context, deepLinkHandlers, deepLinkPrefixes) {
        val navState = viewModel.globalSavedStateHandle.get<Bundle>(SAVED_STATE_STACK)

        if (navState?.getBoolean(SAVED_STATE_HANDLED_DEEP_LINKS) != true) {
            deepLinkRoutes(context, deepLinkHandlers, deepLinkPrefixes)
        } else {
            emptyList()
        }
    }

    return remember(stack, viewModel, starter, deepLinkRoutes) {
        MultiStackNavigationExecutor(
            stack = stack,
            viewModel = viewModel,
            activityStarter = starter::start,
            deepLinkRoutes = deepLinkRoutes,
        )
    }
}

private fun deepLinkRoutes(
    context: Context,
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
): List<Parcelable> {
    var intent = context.findActivity().intent
    val uri = intent.dataString
    if (uri != null) {
        val deepLink = deepLinkHandlers.createDeepLinkIfMatching(Uri.parse(uri), deepLinkPrefixes)
        val deepLinkIntent = deepLink?.buildIntent(context)
        if (deepLinkIntent != null) {
            intent = deepLinkIntent
        }
    }
    @Suppress("DEPRECATION")
    return intent.getParcelableArrayListExtra(EXTRA_DEEPLINK_ROUTES) ?: emptyList()
}
