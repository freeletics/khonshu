package com.freeletics.mad.navigator

/**
 * DeepLinkHandler can be used to implement Uri based deep links.
 */
public interface DeepLinkHandler {
    /**
     * Prefixes that are used together with the patterns and should contain the scheme and host
     * for the urls to be handled. This is optional and if left empty the defaults of the app
     * are used.
     *
     * For example:
     * - `https://example.com`
     * - `app://example.com`
     */
    public val prefixes: Set<Prefix>
        get(): Set<Prefix> = emptySet()

    /**
     * Url patterns handled by this deep link. The pattern is not allowed to start with a `/`. It
     * also can not contain any query parameters which are ignored for the purposed of matching
     * (any query parameter in the actual url will still be passed to [deepLink]. Placeholders
     * can be added to the pattern with curly braces.
     *
     * For example:
     * - `coach`
     * - `users/{id}/achievements`
     * - `users/{user_id}/achievements{badge_id}`
     */
    public val patterns: Set<Pattern>

    /**
     * Build the [DeepLink] to the screen.
     *
     * For the pattern `{locale}/test/users/{id}` the [pathParameters] would contain
     * `locale` and `id` keys.
     * [queryParameters] contains all query parameters that were present in the url.
     */
    public fun deepLink(
        pathParameters: Map<String, String>,
        queryParameters: Map<String, String>
    ): DeepLink


    /**
     * A url prefix consisting of scheme and host. This is not allowed to have a trailing / or other
     * url elements.
     */
    @JvmInline
    public value class Prefix(internal val value: String) {
        init {
            check(PREFIX_REGEX.matches(value)) { "$value does not match ${PREFIX_REGEX.pattern}" }
        }
    }

    /**
     * A deep link pattern to be used in [DeepLinkHandler.patterns]. The pattern is a relative path
     * and should not start with a leading `/`. Query parameters are not allowed to be included.
     *
     * The path can contain placeholders surrounded by curly brackets, i.e. `foo/{id}/bar`
     * would result in a placeholder named `id`. Placeholders are required to be a full path
     * segment.
     */
    @JvmInline
    public value class Pattern(internal val value: String) {
        init {
            check(!value.startsWith("/")) { "Pattern should not start with a / but is $value" }
            check(!value.contains("?")) { "Pattern should not contain any query parameters is $value" }
        }
    }

    private companion object {
        private val PREFIX_REGEX = "[a-z]+://[a-z0-9._-]+.[a-z]+".toRegex()
    }
}
