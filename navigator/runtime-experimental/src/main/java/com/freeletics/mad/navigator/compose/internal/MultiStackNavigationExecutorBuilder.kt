package com.freeletics.mad.navigator.compose.internal

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eygraber.uri.Uri
import com.freeletics.mad.navigator.DeepLink.Companion.EXTRA_DEEPLINK_ROUTES
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.compose.ActivityDestination
import com.freeletics.mad.navigator.compose.ContentDestination
import com.freeletics.mad.navigator.compose.NavDestination
import com.freeletics.mad.navigator.compose.findActivity
import com.freeletics.mad.navigator.compose.internal.MultiStackNavigationExecutor.Companion.SAVED_STATE_STACK
import com.freeletics.mad.navigator.internal.createDeepLinkIfMatching

@Composable
internal fun rememberNavigationExecutor(
    startRoot: NavRoot,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
): MultiStackNavigationExecutor {
    val context = LocalContext.current
    val viewModel = viewModel<StoreViewModel>(factory = SavedStateViewModelFactory())
    return remember(context, viewModel) {
        val contentDestinations = destinations.filterIsInstance<ContentDestination<*>>()
        val activityDestinations = destinations.filterIsInstance<ActivityDestination>()

        val navState = viewModel.globalSavedStateHandle.get<Bundle>(SAVED_STATE_STACK)
        val stack = if (navState == null) {
            MultiStack.createWith(startRoot, contentDestinations, viewModel::removeEntry)
        } else {
            MultiStack.fromState(startRoot, navState, contentDestinations, viewModel::removeEntry)
        }

        val starter = ActivityStarter(context, activityDestinations)

        val deepLinkRoutes = if (navState?.getBoolean(SAVED_STATE_STACK) != true) {
            deepLinkRoutes(context, deepLinkHandlers, deepLinkPrefixes)
        } else {
            emptyList()
        }

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
