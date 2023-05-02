package com.freeletics.mad.navigator.test

import com.freeletics.mad.navigator.DeepLink
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.DeepLinkHandler.Pattern
import com.freeletics.mad.navigator.DeepLinkHandler.Prefix

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
