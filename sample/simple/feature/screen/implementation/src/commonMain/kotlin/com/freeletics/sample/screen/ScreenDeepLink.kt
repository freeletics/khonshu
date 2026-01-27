package com.freeletics.sample.screen

import com.freeletics.khonshu.navigation.deeplinks.DeepLink
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.sample.root.nav.RootRoute
import com.freeletics.sample.screen.nav.ScreenRoute
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class ScreenDeepLink() : DeepLinkHandler {
    override val patterns = setOf(
        DeepLinkHandler.Pattern("screen/{screenNum}"),
    )

    override fun deepLink(pathParameters: Map<String, String>, queryParameters: Map<String, String>): DeepLink {
        return DeepLink(root = RootRoute, listOf(ScreenRoute(pathParameters["screenNum"]!!.toInt())))
    }
}
