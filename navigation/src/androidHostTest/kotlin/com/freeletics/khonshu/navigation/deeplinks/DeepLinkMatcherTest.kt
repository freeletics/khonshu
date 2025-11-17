package com.freeletics.khonshu.navigation.deeplinks

import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.test.DeepLinkHandlerSubject.Companion.assertThat
import com.freeletics.khonshu.navigation.test.TestDeepLinkHandler
import com.google.common.truth.Truth.assertThat
import java.util.regex.PatternSyntaxException
import org.junit.Assert
import org.junit.Test

internal class DeepLinkMatcherTest {
    private val prefixes: Set<DeepLinkHandler.Prefix> = setOf(
        DeepLinkHandler.Prefix("https://a.com"),
        DeepLinkHandler.Prefix("https://b.de"),
        DeepLinkHandler.Prefix("app://b.de"),
        DeepLinkHandler.Prefix("app://a.b.de"),
    )

    @Test
    fun `when the pattern is home`() {
        val handler = TestDeepLinkHandler("home")

        assertThat(handler).createDeepLinkIfMatching("https://a.com/home", prefixes).isNotNull() // first prefix
        assertThat(handler).createDeepLinkIfMatching("https://b.de/home", prefixes).isNotNull() // second prefix
        assertThat(handler).createDeepLinkIfMatching("app://b.de/home", prefixes).isNotNull() // third prefix
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/home?param1=value1&param2=value2", prefixes)
            .isNotNull() // with query parameters

        assertThat(handler).createDeepLinkIfMatching("https://a.com", prefixes).isNull() // just the prefix 1
        assertThat(handler).createDeepLinkIfMatching("https://a.com/", prefixes).isNull() // just the prefix 2
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/home/world", prefixes).isNull() // different path
        assertThat(handler).createDeepLinkIfMatching("https://a.com/home/", prefixes)
            .isNull() // trailing slash that is not in pattern
        assertThat(handler).createDeepLinkIfMatching("https://c.com/home", prefixes).isNull() // different domain
        assertThat(handler).createDeepLinkIfMatching("https://a.abc/home", prefixes).isNull() // different tld
        assertThat(handler).createDeepLinkIfMatching("web://b.de/home", prefixes).isNull() // different scheme
    }

    @Test
    fun `when the pattern is empty`() {
        val handler = TestDeepLinkHandler("")

        assertThat(handler).createDeepLinkIfMatching("https://a.com/", prefixes).isNotNull() // first prefix
        assertThat(handler).createDeepLinkIfMatching("https://b.de/", prefixes).isNotNull() // second prefix
        assertThat(handler).createDeepLinkIfMatching("app://b.de/", prefixes).isNotNull() // third prefix
        assertThat(handler).createDeepLinkIfMatching("https://a.com", prefixes).isNotNull() // first prefix
        assertThat(handler).createDeepLinkIfMatching("https://b.de", prefixes).isNotNull() // second prefix
        assertThat(handler).createDeepLinkIfMatching("app://b.de", prefixes).isNotNull() // third prefix
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com?param1=value1&param2=value2", prefixes)
            .isNotNull() // with query parameters
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/?param1=value1&param2=value2", prefixes)
            .isNotNull() // with query parameters

        assertThat(handler).createDeepLinkIfMatching("https://a.com/home", prefixes).isNull() // different path
    }

    @Test
    fun `when the pattern is foo_bar_placeholder`() {
        val handler = TestDeepLinkHandler("foo/bar/{placeholder}")

        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/bar/abc", prefixes).isNotNull() // lower case chars
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/bar/ABC", prefixes).isNotNull() // upper case chars
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/bar/123", prefixes).isNotNull() // numbers
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/bar/_'!+~=,-.@\$:", prefixes)
            .isNotNull() // all allowed special character
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/bar/aBC123_=", prefixes)
            .isNotNull() // combination of the above
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/bar/abc?param1=value1&param2=value2", prefixes)
            .isNotNull() // with query parameters

        assertThat(handler).createDeepLinkIfMatching("https://a.com/foo/bar/", prefixes).isNull() // empty placeholder
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/bar/§&", prefixes)
            .isNull() // not allowed special characters
    }

    @Test
    fun `when the pattern is placeholder_foo_bar`() {
        val handler = TestDeepLinkHandler("{placeholder}/foo/bar")

        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/abc/foo/bar", prefixes).isNotNull() // lower case chars
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/ABC/foo/bar", prefixes).isNotNull() // upper case chars
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/123/foo/bar", prefixes).isNotNull() // numbers
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/_'!+~=,-.@\$:/foo/bar", prefixes)
            .isNotNull() // all allowed special character
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/aBC123_=/foo/bar", prefixes)
            .isNotNull() // combination of the above
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/abc/foo/bar?param1=value1&param2=value2", prefixes)
            .isNotNull() // with query parameters

        assertThat(handler).createDeepLinkIfMatching("https://a.com//foo/bar", prefixes).isNull() // empty placeholder
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/§&/foo/bar", prefixes)
            .isNull() // not allowed special characters
    }

    @Test
    fun `when the pattern is foo_placeholder_bar`() {
        val handler = TestDeepLinkHandler("foo/{placeholder}/bar")

        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/abc/bar", prefixes).isNotNull() // lower case chars
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/ABC/bar", prefixes).isNotNull() // upper case chars
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/123/bar", prefixes).isNotNull() // numbers
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/_'!+~=,-.@\$:/bar", prefixes)
            .isNotNull() // all allowed special character
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/aBC123_=/bar", prefixes)
            .isNotNull() // combination of the above
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/abc/bar?param1=value1&param2=value2", prefixes)
            .isNotNull() // with query parameters

        assertThat(handler).createDeepLinkIfMatching("https://a.com/foo//bar", prefixes).isNull() // empty placeholder
        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/§&/bar", prefixes)
            .isNull() // not allowed special characters
    }

    @Test
    fun `when the pattern is foo_placeholder1_bar_placeholder2`() {
        val handler = TestDeepLinkHandler("foo/{placeholder1}/bar/{placeholder2}")

        assertThat(handler)
            .createDeepLinkIfMatching("https://a.com/foo/abc/bar/aBC123_=", prefixes).isNotNull()
    }

    @Test
    fun `when the pattern has an invalid placeholder`() {
        val handler = TestDeepLinkHandler("foo/a{placeholder}/bar")

        val exception = Assert.assertThrows(PatternSyntaxException::class.java) {
            setOf(handler).createDeepLinkIfMatching(Uri.parse("https://a.com/foo/abc/bar"), prefixes)
        }
        assertThat(exception).hasMessageThat().startsWith("Illegal repetition near index ")
    }

    @Test
    fun `when the prefix is https_test_com`() {
        val handler = TestDeepLinkHandler(
            patterns = setOf(DeepLinkHandler.Pattern("home")),
            prefixes = setOf(DeepLinkHandler.Prefix("https://test.com")),
        )

        assertThat(handler).createDeepLinkIfMatching(Uri.parse("https://test.com/home"), prefixes).isNotNull()
        assertThat(handler)
            .createDeepLinkIfMatching("https://test.com/home?param1=value1&param2=value2", prefixes)
            .isNotNull() // with query parameters

        assertThat(handler).createDeepLinkIfMatching("https://a.com/home", prefixes).isNull() // first default prefix
        assertThat(handler).createDeepLinkIfMatching("https://b.de/home", prefixes).isNull() // second default prefix
        assertThat(handler).createDeepLinkIfMatching("app://b.de/home", prefixes).isNull() // third default prefix
    }

    @Test
    fun `for a set of deep links, returns true if one matches`() {
        val handlers = setOf(
            TestDeepLinkHandler(
                patterns = setOf(DeepLinkHandler.Pattern("home")),
            ),
            TestDeepLinkHandler(""),
        )

        val uri = Uri.parse("https://a.com/home")
        assertThat(handlers.createDeepLinkIfMatching(uri, prefixes)).isNotNull()
    }

    @Test
    fun `for a set of deep links, returns false if none matches`() {
        val handlers = setOf(
            TestDeepLinkHandler(
                patterns = setOf(DeepLinkHandler.Pattern("home")),
            ),
            TestDeepLinkHandler(""),
        )

        val uri = Uri.parse("https://a.com/foo")
        assertThat(handlers.createDeepLinkIfMatching(uri, prefixes)).isNull()
    }
}
