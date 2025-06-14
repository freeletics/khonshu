package com.freeletics.khonshu.navigation.deeplinks

import android.content.Intent
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavDestination
import kotlinx.collections.immutable.ImmutableSet

/**
 * If the given [Intent] was created from a [DeepLink] or the `Uri` returned by [Intent.getData]
 * can be handled using [deepLinkHandlers] and [deepLinkPrefixes] then the navigator will
 * clear the current back stack and navigate to the required destinations.
 *
 * Returns `true` if the `Intent` contained a deeplink that was handled.
 */
public fun HostNavigator.handleDeepLink(
    intent: Intent,
    destinations: ImmutableSet<NavDestination<*>>,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
): Boolean {
    return handleDeepLink(intent.asLaunchInfo(destinations), deepLinkHandlers, deepLinkPrefixes)
}
