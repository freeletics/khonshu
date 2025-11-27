package com.freeletics.khonshu.navigation.deeplinks

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.peanuuutz.tomlkt.Toml

@Serializable
public data class DeepLinkDefinitions(
    val prefixes: List<PrefixDefinition> = emptyList(),
    val placeholders: List<PlaceholderDefinition> = emptyList(),
    val deepLinks: Map<String, DeepLinkDefinition>,
) {
    public companion object {
        public fun decodeFromString(string: String): DeepLinkDefinitions {
            return Toml.decodeFromString<DeepLinkDefinitions>(string)
        }
    }
}

@Serializable
public data class DeepLinkDefinition(
    val patterns: List<PatternDefinition>,
    val prefixes: List<PrefixDefinition> = emptyList(), // use global if empty
    val placeholders: List<PlaceholderDefinition> = emptyList(), // use global if empty or if key not found
    val exampleQueries: List<String> = emptyList(),
)

@Serializable
@JvmInline
public value class PatternDefinition(
    public val value: String,
) {
    init {
        // for validation purposes
        DeepLinkHandler.Pattern(value)
    }
}

@Serializable
public data class PrefixDefinition(
    val scheme: String,
    val host: String,
    val autoVerified: Boolean,
) {
    init {
        // for validation purposes
        DeepLinkHandler.Prefix("$scheme://$host")
    }
}

@Serializable
public data class PlaceholderDefinition(
    val key: String,
    val exampleValues: List<String>,
)
