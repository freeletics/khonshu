package com.freeletics.khonshu.navigation.internal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.eygraber.uri.toUri
import com.freeletics.khonshu.navigation.ActivityDestination
import com.freeletics.khonshu.navigation.ContentDestination
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.EXTRA_DEEPLINK_ROUTES
import com.freeletics.khonshu.navigation.deeplinks.createDeepLinkIfMatching
import com.freeletics.khonshu.navigation.internal.MultiStackHostNavigator.Companion.SAVED_STATE_HANDLED_DEEP_LINKS
import com.freeletics.khonshu.navigation.internal.MultiStackHostNavigator.Companion.SAVED_STATE_STACK
import kotlinx.collections.immutable.ImmutableSet

internal fun createHostNavigator(
    context: Context,
    intent: Intent,
    viewModel: StackEntryStoreViewModel,
    startRoot: NavRoot,
    destinations: ImmutableSet<NavDestination>,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
): MultiStackHostNavigator {
    val activityDestinations = destinations.filterIsInstance<ActivityDestination>()
    val starter = ActivityStarter(context.applicationContext, activityDestinations)

    val contentDestinations = destinations.filterIsInstance<ContentDestination<*>>()
    val factory = StackEntryFactory(contentDestinations, viewModel)

    val navState = viewModel.globalSavedStateHandle.get<Bundle>(SAVED_STATE_STACK)
    val stack = if (navState == null) {
        MultiStack.createWith(
            root = startRoot,
            createEntry = factory::create,
        )
    } else {
        MultiStack.fromState(
            root = startRoot,
            bundle = navState,
            createEntry = factory::create,
            createRestoredEntry = factory::create,
        )
    }

    val deepLinkRoutes = if (navState?.getBoolean(SAVED_STATE_HANDLED_DEEP_LINKS) != true) {
        deepLinkRoutes(intent, deepLinkHandlers, deepLinkPrefixes)
    } else {
        emptyList()
    }

    return MultiStackHostNavigator(
        stack = stack,
        viewModel = viewModel,
        activityStarter = starter::start,
        deepLinkRoutes = deepLinkRoutes,
    )
}

private fun deepLinkRoutes(
    intent: Intent,
    deepLinkHandlers: Set<DeepLinkHandler>,
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix>,
): List<Parcelable> {
    if (intent.hasExtra(EXTRA_DEEPLINK_ROUTES)) {
        @Suppress("DEPRECATION")
        return intent.getParcelableArrayListExtra(EXTRA_DEEPLINK_ROUTES)!!
    }
    val uri = intent.data
    if (uri != null) {
        val deepLink = deepLinkHandlers.createDeepLinkIfMatching(uri.toUri(), deepLinkPrefixes)
        return deepLink?.routes ?: emptyList()
    }
    return emptyList()
}
