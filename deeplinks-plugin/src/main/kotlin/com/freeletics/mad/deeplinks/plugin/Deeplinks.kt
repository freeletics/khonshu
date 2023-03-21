package com.freeletics.mad.deeplinks.plugin

internal data class DeepLinks(
    val prefixes: List<Prefix>,
    val placeholders: List<Placeholder>,
    val deepLinks: Map<String, DeepLink>,
)
internal data class DeepLink(
    val patterns: List<String>,
    val prefixes: List<Prefix>?, // use global if null
    val placeholders: List<Placeholder>?, // use global if null or if key not found
)

internal data class Prefix(
    val value: String,
    val autoVerified: Boolean,
)

internal data class Placeholder(
    val key: String,
    val values: List<String>,
)