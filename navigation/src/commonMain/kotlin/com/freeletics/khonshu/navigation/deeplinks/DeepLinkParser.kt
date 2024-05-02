package com.freeletics.khonshu.navigation.deeplinks

import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Prefix
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import org.jetbrains.annotations.VisibleForTesting

/**
 * Checks if the given [uri] matches one of the [DeepLinkHandler] in the `Set` and if yes
 * creates a [DeepLink]. Otherwise `null` is returned.
 *
 * [defaultPrefixes] will be used as base url if [DeepLinkHandler.prefixes] is empty.
 */
@InternalNavigationApi
public fun Set<DeepLinkHandler>.createDeepLinkIfMatching(
    uri: Uri,
    defaultPrefixes: Set<Prefix>,
): DeepLink? {
    forEach {
        val matchingPattern = it.findMatchingPattern(uri.toString(), defaultPrefixes)
        if (matchingPattern != null) {
            return it.deepLink(matchingPattern.extractPathParameters(uri), uri.queryParameters)
        }
    }

    return null
}

@VisibleForTesting
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

private val Uri.queryParameters: Map<String, String>
    get() = getQueryParameterNames().associateWith { queryParam ->
        getQueryParameter(queryParam)!!
    }
