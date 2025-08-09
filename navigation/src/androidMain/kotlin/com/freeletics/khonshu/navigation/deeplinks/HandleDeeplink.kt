package com.freeletics.khonshu.navigation.deeplinks

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.internal.destinationId
import kotlinx.collections.immutable.ImmutableSet

/**
 * If the given [launchInfo] can be handled using [deepLinkHandlers] and [deepLinkPrefixes]
 * then the navigator will clear the current back stack and navigate to the required
 * destinations.
 *
 * Returns `true` if the `launchInfo` contained a deeplink that was handled.
 */
public fun HostNavigator.handleDeepLink(
    launchInfo: LaunchInfo,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
): Boolean {
    val deepLinkRoutes = launchInfo.routes
    if (deepLinkRoutes != null) {
        return handleDeepLink(deepLinkRoutes)
    }

    val uri = launchInfo.uri
    if (uri != null) {
        val deepLink = deepLinkHandlers.createDeepLinkIfMatching(uri, deepLinkPrefixes)
        if (deepLink != null) {
            return handleDeepLink(deepLink.routes)
        }
    }

    return false
}

private fun HostNavigator.handleDeepLink(deepLinkRoutes: List<BaseRoute>): Boolean {
    val root = snapshot.value.startRoot.route
    navigate {
        showRoot(root)

        deepLinkRoutes.forEachIndexed { index, route ->
            when (route) {
                is NavRoot -> {
                    require(index == 0) { "NavRoot can only be the first element of a deep link" }
                    require(route.destinationId != root.destinationId) {
                        "$route is the start root which is not allowed to be part of a deep " +
                            "link because it will always be on the back stack"
                    }
                    showRoot(route)
                }

                is NavRoute -> navigateTo(route)
            }
        }
    }

    return true
}
