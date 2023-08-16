package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.DeepLink
import com.freeletics.khonshu.navigation.DeepLinkHandler
import com.freeletics.khonshu.navigation.DeepLinkHandler.Pattern
import com.freeletics.khonshu.navigation.DeepLinkHandler.Prefix

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
