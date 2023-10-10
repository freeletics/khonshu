package com.freeletics.khonshu.navigation.deeplinks

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class DeepLinkHandlerTest {

    @Test
    fun `fails on invalid prefix values`() {
        listOf(
            "https",
            "https://",
            "example.com",
            "://example.com",
            "https://example.com/",
            "https://example.com/abc",
            "abc/https://example.com",
        ).forEach {
            val exception = assertThrows(IllegalStateException::class.java) {
                DeepLinkHandler.Prefix(it)
            }

            assertThat(exception).hasMessageThat()
                .isEqualTo("$it does not match ^[a-z]+://[a-z0-9._-]+\\.[a-z]+$")
        }
    }

    @Test
    fun `fails on pattern values with leading slash`() {
        listOf(
            "/",
            "/abc",
            "/{abc}/a",
        ).forEach {
            println(it)
            val exception = assertThrows(IllegalStateException::class.java) {
                DeepLinkHandler.Pattern(it)
            }

            assertThat(exception).hasMessageThat()
                .isEqualTo("Pattern should not start with a / but is $it")
        }
    }

    @Test
    fun `fails on invalid pattern values containing question mark`() {
        listOf(
            "?",
            "?abc",
            "abc?",
            "abc?abc",
            "abc?a=b&c=d",
        ).forEach {
            println(it)
            val exception = assertThrows(IllegalStateException::class.java) {
                DeepLinkHandler.Pattern(it)
            }

            assertThat(exception).hasMessageThat()
                .isEqualTo("Pattern should not contain any query parameters but is $it")
        }
    }
}
