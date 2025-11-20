package com.freeletics.khonshu.navigation.deeplinks

import com.google.common.truth.Truth.assertThat
import kotlin.test.assertFailsWith
import org.junit.Test

class DeepLinkTestingTest {
    private val prefix = DeepLinkHandler.Prefix("https://example.com")
    private val handler1 = object : DeepLinkHandler {
        override val patterns: Set<DeepLinkHandler.Pattern> = setOf(DeepLinkHandler.Pattern("abc"))

        override fun deepLink(
            pathParameters: Map<String, String>,
            queryParameters: Map<String, String>,
        ) = DeepLink(emptyList())
    }
    private val handler2 = object : DeepLinkHandler {
        override val patterns: Set<DeepLinkHandler.Pattern> = setOf(DeepLinkHandler.Pattern("abc/{id}"))

        override fun deepLink(
            pathParameters: Map<String, String>,
            queryParameters: Map<String, String>,
        ) = DeepLink(emptyList())
    }

    private val prefixDefinition = PrefixDefinition("https", "example.com", autoVerified = true)
    private val definition1 = DeepLinkDefinition(
        listOf(PatternDefinition("abc")),
    )
    private val definition2 = DeepLinkDefinition(
        listOf(PatternDefinition("abc/{id}")),
        placeholders = listOf(PlaceholderDefinition("id", exampleValues = listOf("1", "2", "3"))),
    )

    @Test
    fun `exact match`() {
        DeepLinkDefinitions(
            prefixes = listOf(prefixDefinition),
            deepLinks = mapOf(
                "link1" to definition1,
                "link2" to definition2,
            ),
        ).containsAllDeepLinks(
            deepLinkHandlers = setOf(handler1, handler2),
            defaultPrefixes = setOf(prefix),
        )
    }

    @Test
    fun `missing definition`() {
        val error = assertFailsWith(AssertionError::class) {
            DeepLinkDefinitions(
                prefixes = listOf(prefixDefinition),
                deepLinks = mapOf(
                    "link1" to definition1,
                ),
            ).containsAllDeepLinks(
                deepLinkHandlers = setOf(handler1, handler2),
                defaultPrefixes = setOf(prefix),
            )
        }

        assertThat(error)
            .hasMessageThat()
            .isEqualTo(
                "The following deep links are not defined in TOML but are present in code: " +
                    "[(Prefix(value=https://example.com), Pattern(value=abc/{id}))]",
            )
    }

    @Test
    fun `missing handler`() {
        val error = assertFailsWith(AssertionError::class) {
            DeepLinkDefinitions(
                prefixes = listOf(prefixDefinition),
                deepLinks = mapOf(
                    "link1" to definition1,
                    "link2" to definition2,
                ),
            ).containsAllDeepLinks(
                deepLinkHandlers = setOf(handler1),
                defaultPrefixes = setOf(prefix),
            )
        }

        assertThat(error).hasMessageThat()
            .isEqualTo(
                "The following deep links are not defined in code but are present in the TOML file: " +
                    "[(Prefix(value=https://example.com), Pattern(value=abc/{id}))]",
            )
    }
}
