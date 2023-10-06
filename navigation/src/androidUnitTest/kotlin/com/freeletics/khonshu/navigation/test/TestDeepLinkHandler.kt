package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.deeplinks.DeepLink
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Pattern
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Prefix

internal class TestDeepLinkHandler(
    override val patterns: Set<Pattern>,
    override val prefixes: Set<Prefix> = emptySet(),
    private val deepLinkFactory: (Map<String, String>, Map<String, String>) -> DeepLink = { _, _ ->
        throw AssertionError("Should never be called")
    },
) : DeepLinkHandler {

    constructor(vararg patterns: String) : this(patterns.map { Pattern(it) }.toSet())

    override fun deepLink(
        pathParameters: Map<String, String>,
        queryParameters: Map<String, String>,
    ): DeepLink {
        return deepLinkFactory(pathParameters, queryParameters)
    }
}
