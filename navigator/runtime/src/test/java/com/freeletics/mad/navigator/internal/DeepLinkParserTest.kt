package com.freeletics.mad.navigator.internal

import com.eygraber.uri.Uri
import com.freeletics.mad.navigator.DeepLink
import com.freeletics.mad.navigator.DeepLinkHandler.Pattern
import com.freeletics.mad.navigator.DeepLinkHandler.Prefix
import com.freeletics.mad.navigator.test.DeepLinkHandlerPatternSubject.Companion.assertThat
import com.freeletics.mad.navigator.test.DeepLinkRoute
import com.freeletics.mad.navigator.test.TestDeepLinkHandler
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class DeepLinkParserTest {
    @Test
    fun `when the pattern is home`() {
        val pattern = Pattern("home")

        assertThat(pattern).extractPathParameters("https://a.com/home").isEmpty()
        assertThat(pattern).extractPathParameters("https://b.de/home").isEmpty()
        assertThat(pattern).extractPathParameters("app://b.de/home").isEmpty()
        assertThat(pattern).extractPathParameters("https://a.com/home?param1=value1&param2=value2").isEmpty()
    }

    @Test
    fun `when the pattern is empty`() {
        val pattern = Pattern("")

        assertThat(pattern).extractPathParameters("https://a.com/").isEmpty()
        assertThat(pattern).extractPathParameters("https://b.de/").isEmpty()
        assertThat(pattern).extractPathParameters("app://b.de/").isEmpty()
        assertThat(pattern).extractPathParameters("https://a.com").isEmpty()
        assertThat(pattern).extractPathParameters("https://b.de").isEmpty()
        assertThat(pattern).extractPathParameters("app://b.de").isEmpty()
        assertThat(pattern).extractPathParameters("https://a.com?param1=value1&param2=value2").isEmpty()
        assertThat(pattern).extractPathParameters("https://a.com/?param1=value1&param2=value2").isEmpty()
    }

    @Test
    fun `when the pattern is foo_bar_placeholder`() {
        val pattern = Pattern("foo/bar/{placeholder}")

        assertThat(pattern).extractPathParameters("https://a.com/foo/bar/abc")
            .containsExactly("placeholder", "abc")
        assertThat(pattern).extractPathParameters("https://a.com/foo/bar/ABC")
            .containsExactly("placeholder", "ABC")
        assertThat(pattern).extractPathParameters("https://a.com/foo/bar/123")
            .containsExactly("placeholder", "123")
        assertThat(pattern).extractPathParameters("https://a.com/foo/bar/_'!+~=,-.@\$:")
            .containsExactly("placeholder", "_'!+~=,-.@\$:")
        assertThat(pattern).extractPathParameters("https://a.com/foo/bar/aBC123_=")
            .containsExactly("placeholder", "aBC123_=")
        assertThat(pattern).extractPathParameters("https://a.com/foo/bar/abc?param1=value1&param2=value2")
            .containsExactly("placeholder", "abc")
    }

    @Test
    fun `when the pattern is placeholder_foo_bar`() {
        val pattern = Pattern("{placeholder}/foo/bar")

        assertThat(pattern).extractPathParameters("https://a.com/abc/foo/bar")
            .containsExactly("placeholder", "abc")
        assertThat(pattern).extractPathParameters("https://a.com/ABC/foo/bar")
            .containsExactly("placeholder", "ABC")
        assertThat(pattern).extractPathParameters("https://a.com/123/foo/bar")
            .containsExactly("placeholder", "123")
        assertThat(pattern).extractPathParameters("https://a.com/_'!+~=,-.@\$:/foo/bar")
            .containsExactly("placeholder", "_'!+~=,-.@\$:")
        assertThat(pattern).extractPathParameters("https://a.com/aBC123_=/foo/bar")
            .containsExactly("placeholder", "aBC123_=")
        assertThat(pattern).extractPathParameters("https://a.com/abc/foo/bar?param1=value1&param2=value2")
            .containsExactly("placeholder", "abc")
    }

    @Test
    fun `when the pattern is foo_placeholder_bar`() {
        val pattern = Pattern("foo/{placeholder}/bar")

        assertThat(pattern).extractPathParameters("https://a.com/foo/abc/bar")
            .containsExactly("placeholder", "abc")
        assertThat(pattern).extractPathParameters("https://a.com/foo/ABC/bar")
            .containsExactly("placeholder", "ABC")
        assertThat(pattern).extractPathParameters("https://a.com/foo/123/bar")
            .containsExactly("placeholder", "123")
        assertThat(pattern).extractPathParameters("https://a.com/foo/_'!+~=,-.@\$:/bar")
            .containsExactly("placeholder", "_'!+~=,-.@\$:")
        assertThat(pattern).extractPathParameters("https://a.com/foo/aBC123_=/bar")
            .containsExactly("placeholder", "aBC123_=")
        assertThat(pattern).extractPathParameters("https://a.com/foo/abc/bar?param1=value1&param2=value2")
            .containsExactly("placeholder", "abc")
    }

    @Test
    fun `when the pattern is foo_placeholder1_bar_placeholder2`() {
        val pattern = Pattern("foo/{placeholder1}/bar/{placeholder2}")

        assertThat(pattern).extractPathParameters("https://a.com/foo/abc/bar/aBC123_=")
            .containsExactly("placeholder1", "abc", "placeholder2", "aBC123_=")
    }

    @Test
    fun `for a set of deep links, returns deep link if one matches`() {
        val handlers = setOf(
            TestDeepLinkHandler(
                patterns = setOf(Pattern("home")),
                prefixes = setOf(Prefix("https://a.com")),
                deepLinkFactory = { _, _ -> DeepLink("test", listOf()) },
            ),
            TestDeepLinkHandler(""),
        )

        val uri = Uri.parse("https://a.com/home")
        assertThat(handlers.createDeepLinkIfMatching(uri, emptySet())).isEqualTo(
            DeepLink("test", listOf()),
        )
    }

    @Test
    fun `for a set of deep links, returns deep link with parameters if one matches`() {
        val handlers = setOf(
            TestDeepLinkHandler(
                patterns = setOf(Pattern("{path}")),
                prefixes = setOf(Prefix("https://a.com")),
                deepLinkFactory = { path, query ->
                    DeepLink("test", listOf(DeepLinkRoute(path, query)))
                },
            ),
            TestDeepLinkHandler(""),
        )

        val uri = Uri.parse("https://a.com/home?query=value&other=true")
        assertThat(handlers.createDeepLinkIfMatching(uri, emptySet())).isEqualTo(
            DeepLink(
                "test",
                listOf(
                    DeepLinkRoute(
                        mapOf("path" to "home"),
                        mapOf("query" to "value", "other" to "true"),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `for a set of deep links, returns null if none matches`() {
        val handlers = setOf(
            TestDeepLinkHandler(
                patterns = setOf(Pattern("home")),
                prefixes = setOf(Prefix("https://a.com")),
            ),
            TestDeepLinkHandler(""),
        )

        val uri = Uri.parse("https://a.com/foo")
        assertThat(handlers.createDeepLinkIfMatching(uri, emptySet())).isNull()
    }
}
