package com.freeletics.mad.navigator.internal

import com.eygraber.uri.Uri
import com.freeletics.mad.navigator.DeepLink
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.DeepLinkHandler.Prefix

/**
 * Checks if the given [uri] matches one of the [DeepLinkHandler] in the `Set` and if yes
 * creates a [DeepLink]. Otherwise `null` is returned.
 *
 * [defaultPrefixes] will be used as base url if [DeepLinkHandler.prefixes] is empty.
 */
@InternalNavigatorApi
public fun Set<DeepLinkHandler>.createDeepLinkIfMatching(
    uri: Uri,
    defaultPrefixes: Set<Prefix>
): DeepLink? {
    forEach {
        val result = it.createDeepLinkIfMatching(uri, defaultPrefixes)
        if (result != null) {
            return result
        }
    }

    return null
}

/**
 * Checks if the given [uri] matches one of [DeepLinkHandler.patterns] and if yes
 * create a [DeepLink]. Otherwise `null` is returned.
 *
 * [defaultPrefixes] will be used as base url if [DeepLinkHandler.prefixes] is empty.
 */
@InternalNavigatorApi
public fun DeepLinkHandler.createDeepLinkIfMatching(
    uri: Uri,
    defaultPrefixes: Set<Prefix>
): DeepLink? {
    val matchingPattern = findMatchingPattern(uri.toString(), defaultPrefixes) ?: return null
    return deepLink(matchingPattern.extractPathParameters(uri), uri.queryParameters)
}

internal fun DeepLinkHandler.Pattern.extractPathParameters(uri: Uri): MutableMap<String, String> {
    // create a Uri with the pattern so that we can get the placeholder names and their indices
    // the prefix (domain) does not matter so we use a hardcoded one
    val patternUri = Uri.parse("https://example.com/$value")

    // this check should never fail since the regex matched
    check(uri.pathSegments.size == patternUri.pathSegments.size) {
        "$uri and $patternUri have a different number of path segments"
    }

    val pathParameters = mutableMapOf<String, String>()
    patternUri.pathSegments.forEachIndexed { index, segment ->
        // when a segment is a placeholder extract the name and get the value from the real uri
        if (segment.startsWith("{") && segment.endsWith("}")) {
            pathParameters[segment.substring(1, segment.length - 1)] = uri.pathSegments[index]
        }
    }

    return pathParameters
}

internal val Uri.queryParameters: Map<String, String>
    get() = getQueryParameterNames().associateWith { queryParam ->
        getQueryParameter(queryParam)!!
    }
