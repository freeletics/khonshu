package com.freeletics.khonshu.navigation.deeplinks

import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Pattern
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler.Prefix
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
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

private fun DeepLinkHandler.findMatchingPattern(
    uriString: String,
    defaultPrefixes: Set<Prefix>,
): Pattern? {
    // if DeepLinkHandler does not define any custom prefix use the default ones
    val prefixes = prefixes.ifEmpty { defaultPrefixes }
    // combine all prefixes into a single regular expression that allows any of them
    val regexPrefix = prefixes.asOneOfRegex()
    // find the pattern that matches uriString
    return patterns.find { pattern ->
        // replace all {name} placeholders in the pattern with a regex placeholder
        val regexPattern = pattern.replacePlaceholders()
        // when the pattern is not empty check for it, otherwise check for the prefixes with
        // an optional trailing slash, query parameters are allowed in both cases
        val regex = if (regexPattern.isNotBlank()) {
            "^$regexPrefix/$regexPattern$QUERY_PARAMETER_REGEX".toRegex()
        } else {
            "^$regexPrefix/?$QUERY_PARAMETER_REGEX".toRegex()
        }
        regex.matches(uriString)
    }
}

// combines the values of all prefixes into a single regex string which matches exactly one
// of the prefixes at once
private fun Set<Prefix>.asOneOfRegex(): String {
    return joinToString(prefix = "(", separator = "|", postfix = ")") {
        Regex.escape(it.value)
    }
}

private fun Pattern.replacePlaceholders(replacement: String = PARAM_VALUE): String {
    // $1 and $3 will add the optional leading and trailing slashes if needed
    return value.replace(PARAM_REGEX, "$1$replacement$3")
}

@InternalNavigationTestingApi
public fun Pattern.replacePlaceholders(replacement: (String) -> String): String {
    // $1 and $3 will add the optional leading and trailing slashes if needed
    return value.replace(PARAM_REGEX) { "${it.groupValues[1]}${replacement(it.groupValues[2])}${it.groupValues[3]}" }
}

// matches placeholders like {locale} or {foo_bar-1}, requires a leading slash and either a trailing
// slash or the end of the  string to avoid that a path segment is not fully filled by the
// placeholder
private val PARAM_REGEX = "(/|^)\\{([a-zA-Z][a-zA-Z0-9_-]*)\\}(/|$)".toRegex()

// a regex for values that are allowed in the path segment that contains the placeholder
private const val PARAM_VALUE = "([a-zA-Z0-9_'!+%~=,\\-\\.\\@\\$\\:]+)"

// the query parameter itself is optional and starts with a question mark, afterwards anything
// is accepted since its not part of the pattern, ends with the end of the whole url
private const val QUERY_PARAMETER_REGEX = "(\\?.+)?$"

@VisibleForTesting
internal fun Pattern.extractPathParameters(uri: Uri): MutableMap<String, String> {
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
