package com.freeletics.mad.navigator.internal

import com.eygraber.uri.Uri
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.DeepLinkHandler.Pattern
import com.freeletics.mad.navigator.DeepLinkHandler.Prefix

/**
 * Checks if any of the given [uri] matches one of [DeepLinkHandler.patterns] and returns `true`
 * if that is the case.
 *
 * [defaultPrefixes] will be used as base url if [DeepLinkHandler.prefixes] is empty.
 */
@InternalNavigatorApi
public fun Set<DeepLinkHandler>.matchesPattern(
    uri: Uri,
    defaultPrefixes: Set<Prefix>
): Boolean {
    return any { it.matchesPattern(uri, defaultPrefixes) }
}

/**
 * Checks if the given [uri] matches one of [DeepLinkHandler.patterns] and returns `true` if that
 * is the case.
 *
 * [defaultPrefixes] will be used as base url if [DeepLinkHandler.prefixes] is empty.
 */
@InternalNavigatorApi
public fun DeepLinkHandler.matchesPattern(
    uri: Uri,
    defaultPrefixes: Set<Prefix>
): Boolean {
    return findMatchingPattern(uri.toString(), defaultPrefixes) != null
}

internal fun DeepLinkHandler.findMatchingPattern(
    uriString: String,
    defaultPrefixes: Set<Prefix>
): Pattern? {
    // if DeepLinkHandler does not define any custom prefix use the default ones
    val prefixes = prefixes.ifEmpty { defaultPrefixes }
    // combine all prefixes into a single regular expression that allows any of them
    val regexPrefix = prefixes.asOneOfRegex()
    // find the pattern that matches uriString
    return patterns.find { pattern ->
        // replace all {name} placeholders in the pattern with a regex placeholder
        val regexPattern = pattern.value.replace(PARAM_REGEX, PARAM_VALUE)
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

// matches placeholders like {locale} or {foo_bar-1}, requires a leading slash and either a trailing
// slash or the end of the  string to avoid that a path segment is not fully filled by the
// placeholder
private val PARAM_REGEX = "(/|^)\\{([a-zA-Z][a-zA-Z0-9_-]*)\\}(/|$)".toRegex()

// a regex for values that are allowed in the path segment that contains the placeholder
// $2 will add the optional trailing / if needed
private const val PARAM_VALUE = "$1([a-zA-Z0-9_'!+%~=,\\-\\.\\@\\$\\:]+)$3"

// the query parameter itself is optional and starts with a question mark, afterwards anything
// is accepted since its not part of the pattern, ends with the end of the whole url
private const val QUERY_PARAMETER_REGEX = "(\\?.+)?$"
