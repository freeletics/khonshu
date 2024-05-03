package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.deeplinks.DeepLink
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Pattern
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Prefix

internal class TestDeepLinkHandler(
    override val patterns: Set<Pattern>,
    override val prefixes: Set<Prefix> = emptySet(),
) : DeepLinkHandler {

    constructor(vararg patterns: String) : this(patterns.map { Pattern(it) }.toSet())

    override fun deepLink(
        pathParameters: Map<String, String>,
        queryParameters: Map<String, String>,
    ): DeepLink {
        return DeepLink("test", listOf(DeepLinkRoute(pathParameters, queryParameters)))
    }
}
