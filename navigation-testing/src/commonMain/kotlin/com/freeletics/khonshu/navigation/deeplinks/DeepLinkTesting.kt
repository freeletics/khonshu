package com.freeletics.khonshu.navigation.deeplinks

import kotlin.test.assertTrue

public fun DeepLinkDefinitions.containsAllDeepLinks(
    deepLinkHandlers: Set<DeepLinkHandler>,
    defaultPrefixes: Set<DeepLinkHandler.Prefix>,
) {
    val codePrefixPatternCombinations = deepLinkHandlers.flatMapTo(HashSet()) { handler ->
        handler.prefixes.ifEmpty { defaultPrefixes }.flatMap { prefix ->
            handler.patterns.map { pattern ->
                prefix to pattern
            }
        }
    }

    val definedPrefixPatternCombinations = deepLinks.values.flatMapTo(HashSet()) { deepLink ->
        deepLink.prefixes.ifEmpty { prefixes }.flatMap { prefix ->
            deepLink.patterns.map { pattern ->
                DeepLinkHandler.Prefix("${prefix.scheme}://${prefix.host}") to
                    DeepLinkHandler.Pattern(pattern.value)
            }
        }
    }

    val codeOnly = codePrefixPatternCombinations - definedPrefixPatternCombinations
    assertTrue(
        codeOnly.isEmpty(),
        "The following deep links are not defined in TOML but are present in code: $codeOnly",
    )
    val tomlOnly = definedPrefixPatternCombinations - codePrefixPatternCombinations
    assertTrue(
        tomlOnly.isEmpty(),
        "The following deep links are not defined in code but are present in the TOML file: $tomlOnly",
    )
}
