package com.freeletics.mad.navigator.internal

import com.eygraber.uri.Uri
import com.freeletics.mad.navigator.DeepLink
import com.freeletics.mad.navigator.DeepLinkHandler.Pattern
import com.freeletics.mad.navigator.DeepLinkHandler.Prefix
import com.freeletics.mad.navigator.test.DeepLinkHandlerSubject.Companion.assertThat
import com.freeletics.mad.navigator.test.TestDeepLinkHandler
import com.google.common.truth.Truth.assertThat
import java.util.regex.PatternSyntaxException
import org.junit.Assert.assertThrows
import org.junit.Test

internal class DeepLinkMatcherTest {
    private val prefixes: Set<Prefix> = setOf(
        Prefix("https://a.com"),
        Prefix("https://b.de"),
        Prefix("app://b.de"),
        Prefix("app://a.b.de"),
    )

    @Test
    fun `when the pattern is home`() {
        val handler = TestDeepLinkHandler("home")

        assertThat(handler).matchesPattern("https://a.com/home", prefixes).isTrue() // first prefix
        assertThat(handler).matchesPattern("https://b.de/home", prefixes).isTrue() // second prefix
        assertThat(handler).matchesPattern("app://b.de/home", prefixes).isTrue() // third prefix
        assertThat(handler).matchesPattern("https://a.com/home?param1=value1&param2=value2", prefixes).isTrue() // with query parameters

        assertThat(handler).matchesPattern("https://a.com", prefixes).isFalse() // just the prefix 1
        assertThat(handler).matchesPattern("https://a.com/", prefixes).isFalse() // just the prefix 2
        assertThat(handler).matchesPattern("https://a.com/home/world", prefixes).isFalse() // different path
        assertThat(handler).matchesPattern("https://a.com/home/", prefixes).isFalse() // trailing slash that is not in pattern
        assertThat(handler).matchesPattern("https://c.com/home", prefixes).isFalse() // different domain
        assertThat(handler).matchesPattern("https://a.abc/home", prefixes).isFalse() // different tld
        assertThat(handler).matchesPattern("web://b.de/home", prefixes).isFalse() // different scheme
    }

    @Test
    fun `when the pattern is empty`() {
        val handler = TestDeepLinkHandler("")

        assertThat(handler).matchesPattern("https://a.com/", prefixes).isTrue() // first prefix
        assertThat(handler).matchesPattern("https://b.de/", prefixes).isTrue() // second prefix
        assertThat(handler).matchesPattern("app://b.de/", prefixes).isTrue() // third prefix
        assertThat(handler).matchesPattern("https://a.com", prefixes).isTrue() // first prefix
        assertThat(handler).matchesPattern("https://b.de", prefixes).isTrue() // second prefix
        assertThat(handler).matchesPattern("app://b.de", prefixes).isTrue() // third prefix
        assertThat(handler).matchesPattern("https://a.com?param1=value1&param2=value2", prefixes).isTrue() // with query parameters
        assertThat(handler).matchesPattern("https://a.com/?param1=value1&param2=value2", prefixes).isTrue() // with query parameters

        assertThat(handler).matchesPattern("https://a.com/home", prefixes).isFalse() // different path
    }

    @Test
    fun `when the pattern is foo_bar_placeholder`() {
        val handler = TestDeepLinkHandler("foo/bar/{placeholder}")

        assertThat(handler).matchesPattern("https://a.com/foo/bar/abc", prefixes).isTrue() // lower case chars
        assertThat(handler).matchesPattern("https://a.com/foo/bar/ABC", prefixes).isTrue() // upper case chars
        assertThat(handler).matchesPattern("https://a.com/foo/bar/123", prefixes).isTrue() // numbers
        assertThat(handler).matchesPattern("https://a.com/foo/bar/_'!+~=,-.@\$:", prefixes).isTrue() // all allowed special character
        assertThat(handler).matchesPattern("https://a.com/foo/bar/aBC123_=", prefixes).isTrue() // combination of the above
        assertThat(handler).matchesPattern("https://a.com/foo/bar/abc?param1=value1&param2=value2", prefixes).isTrue() // with query parameters

        assertThat(handler).matchesPattern("https://a.com/foo/bar/", prefixes).isFalse() // empty placeholder
        assertThat(handler).matchesPattern("https://a.com/foo/bar/§&", prefixes).isFalse() // not allowed special characters
    }

    @Test
    fun `when the pattern is placeholder_foo_bar`() {
        val handler = TestDeepLinkHandler("{placeholder}/foo/bar")

        assertThat(handler).matchesPattern("https://a.com/abc/foo/bar", prefixes).isTrue() // lower case chars
        assertThat(handler).matchesPattern("https://a.com/ABC/foo/bar", prefixes).isTrue() // upper case chars
        assertThat(handler).matchesPattern("https://a.com/123/foo/bar", prefixes).isTrue() // numbers
        assertThat(handler).matchesPattern("https://a.com/_'!+~=,-.@\$:/foo/bar", prefixes).isTrue() // all allowed special character
        assertThat(handler).matchesPattern("https://a.com/aBC123_=/foo/bar", prefixes).isTrue() // combination of the above
        assertThat(handler).matchesPattern("https://a.com/abc/foo/bar?param1=value1&param2=value2", prefixes).isTrue() // with query parameters

        assertThat(handler).matchesPattern("https://a.com//foo/bar", prefixes).isFalse() // empty placeholder
        assertThat(handler).matchesPattern("https://a.com/§&/foo/bar", prefixes).isFalse() // not allowed special characters
    }

    @Test
    fun `when the pattern is foo_placeholder_bar`() {
        val handler = TestDeepLinkHandler("foo/{placeholder}/bar")

        assertThat(handler).matchesPattern("https://a.com/foo/abc/bar", prefixes).isTrue() // lower case chars
        assertThat(handler).matchesPattern("https://a.com/foo/ABC/bar", prefixes).isTrue() // upper case chars
        assertThat(handler).matchesPattern("https://a.com/foo/123/bar", prefixes).isTrue() // numbers
        assertThat(handler).matchesPattern("https://a.com/foo/_'!+~=,-.@\$:/bar", prefixes).isTrue() // all allowed special character
        assertThat(handler).matchesPattern("https://a.com/foo/aBC123_=/bar", prefixes).isTrue() // combination of the above
        assertThat(handler).matchesPattern("https://a.com/foo/abc/bar?param1=value1&param2=value2", prefixes).isTrue() // with query parameters

        assertThat(handler).matchesPattern("https://a.com/foo//bar", prefixes).isFalse() // empty placeholder
        assertThat(handler).matchesPattern("https://a.com/foo/§&/bar", prefixes).isFalse() // not allowed special characters
    }

    @Test
    fun `when the pattern is foo_placeholder1_bar_placeholder2`() {
        val handler = TestDeepLinkHandler("foo/{placeholder1}/bar/{placeholder2}")

        assertThat(handler).matchesPattern("https://a.com/foo/abc/bar/aBC123_=", prefixes).isTrue()
    }

    @Test
    fun `when the pattern has an invalid placeholder`() {
        val handler = TestDeepLinkHandler("foo/a{placeholder}/bar")

        val exception = assertThrows(PatternSyntaxException::class.java) {
            handler.matchesPattern(Uri.parse("https://a.com/foo/abc/bar"), prefixes)
        }
        assertThat(exception).hasMessageThat().startsWith("Illegal repetition near index ")
    }

    @Test
    fun `when the prefix is https_test_com`() {
        val handler = TestDeepLinkHandler(
            patterns = setOf(Pattern("home")),
            prefixes = setOf(Prefix("https://test.com"))
        )

        assertThat(handler.matchesPattern(Uri.parse("https://test.com/home"), prefixes)).isTrue()
        assertThat(handler).matchesPattern("https://test.com/home?param1=value1&param2=value2", prefixes).isTrue() // with query parameters

        assertThat(handler).matchesPattern("https://a.com/home", prefixes).isFalse() // first default prefix
        assertThat(handler).matchesPattern("https://b.de/home", prefixes).isFalse() // second default prefix
        assertThat(handler).matchesPattern("app://b.de/home", prefixes).isFalse() // third default prefix
    }

    @Test
    fun `for a set of deep links, returns true if one matches`() {
        val handlers = setOf(
            TestDeepLinkHandler(
                patterns = setOf(Pattern("home")),
                deepLinkFactory = { _, _ -> DeepLink("test", listOf()) },
            ),
            TestDeepLinkHandler("")
        )

        val uri = Uri.parse("https://a.com/home")
        assertThat(handlers.matchesPattern(uri, prefixes)).isTrue()
    }

    @Test
    fun `for a set of deep links, returns false if none matches`() {
        val handlers = setOf(
            TestDeepLinkHandler(
                patterns = setOf(Pattern("home")),
            ),
            TestDeepLinkHandler("")
        )

        val uri = Uri.parse("https://a.com/foo")
        assertThat(handlers.matchesPattern(uri, prefixes)).isFalse()
    }
}
