package com.freeletics.khonshu.sample.feature.screen

import com.freeletics.khonshu.sample.feature.root.nav.RootRoute
import com.freeletics.khonshu.sample.feature.screen.nav.ScreenRoute
import com.freeletics.khonshu.navigation.deeplinks.DeepLink
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
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
