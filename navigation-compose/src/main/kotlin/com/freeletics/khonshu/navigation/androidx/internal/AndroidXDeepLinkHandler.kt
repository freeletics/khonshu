package com.freeletics.khonshu.navigation.androidx.internal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavController.Companion.KEY_DEEP_LINK_ARGS
import androidx.navigation.NavController.Companion.KEY_DEEP_LINK_IDS
import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.deeplinks.DeepLink
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.EXTRA_DEEPLINK_ROUTES
import com.freeletics.khonshu.navigation.deeplinks.buildIntent
import com.freeletics.khonshu.navigation.deeplinks.createDeepLinkIfMatching
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi

/**
 * Handles 2 different kind of deep links:
 * - url based deep links when the `Activity` was launched with a standard `ACTION_VIEW` `Intent`
 *   that contains the url in [Intent.getData]
 * - programmatically launched deep links when the `Activity` was launched with an `Intent` created
 *   through one of the `build...` methods on [DeepLink]
 *
 * In the case of url based deep links the given [deepLinkHandlers] will be searched for a match
 * through which a [DeepLink] will be created. An `Intent` created with [DeepLink.buildIntent] will
 * then replace the original [Activity.getIntent].
 *
 * For programmatically launched deep links the current `Intent` will be augmented with additional
 * information for AndroidX.
 */
@InternalNavigationApi
public fun Activity.handleDeepLink(
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
) {
    if (intent.hasExtra(KEY_DEEP_LINK_IDS)) {
        return
    }
    val uri = intent.dataString
    if (uri != null) {
        val deepLink = deepLinkHandlers.createDeepLinkIfMatching(Uri.parse(uri), deepLinkPrefixes)
        val deepLinkIntent = deepLink?.buildIntent(this)
        if (deepLinkIntent != null) {
            deepLinkIntent.setNavDeepLinkExtras()
            intent = deepLinkIntent
        }
    } else {
        intent.setNavDeepLinkExtras()
    }
}

private fun Intent.setNavDeepLinkExtras() {
    @Suppress("DEPRECATION")
    val routes = getParcelableArrayListExtra<Parcelable>(EXTRA_DEEPLINK_ROUTES)
    if (routes != null) {
        val extra = 1
        val deepLinkIds = IntArray(routes.size + extra)
        val deepLinkArgs = ArrayList<Bundle>(routes.size + extra)
        // represents the nav graph, we always create it with 0 as id
        deepLinkIds[0] = 0
        deepLinkArgs.add(Bundle.EMPTY)

        routes.forEachIndexed { index, route ->
            if (route is BaseRoute) {
                deepLinkIds[index + 1] = route.destinationId()
                deepLinkArgs.add(route.getArguments())
            } else if (route is ActivityRoute) {
                deepLinkIds[index + 1] = route.destinationId()
                deepLinkArgs.add(route.getArguments())
            } else {
                throw IllegalArgumentException("Unknown type of route $route")
            }
        }

        putExtra(KEY_DEEP_LINK_IDS, deepLinkIds)
        putParcelableArrayListExtra(KEY_DEEP_LINK_ARGS, deepLinkArgs)
    }
}
