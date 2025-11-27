package com.freeletics.khonshu.deeplinks.script

import com.freeletics.khonshu.navigation.deeplinks.DeepLinkDefinitions
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.PatternDefinition
import com.freeletics.khonshu.navigation.deeplinks.PlaceholderDefinition
import com.freeletics.khonshu.navigation.deeplinks.replacePlaceholders
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi

internal inline fun DeepLinkDefinitions.forEachDeepLink(
    block: (Int, String, String, List<String>) -> Unit,
) {
    var index = 0
    deepLinks.entries.forEach { (key, deepLinkDefinition) ->
        val placeholders = deepLinkDefinition.placeholders + placeholders
        deepLinkDefinition.prefixes.ifEmpty { prefixes }.forEach { prefix ->
            deepLinkDefinition.patterns.forEach { pattern ->
                val deepLink = "${prefix.scheme}://${prefix.host}/${pattern.value}"

                val placeholderExample = pattern.fillPlaceholders(placeholders)
                val examples = mutableListOf("${prefix.scheme}://${prefix.host}/$placeholderExample")
                deepLinkDefinition.exampleQueries.forEach { query ->
                    examples += "${prefix.scheme}://${prefix.host}/$placeholderExample?$query"
                }

                block(
                    index,
                    key,
                    deepLink,
                    examples,
                )
                index += examples.size
            }
        }
    }
}

@OptIn(InternalNavigationTestingApi::class)
internal fun PatternDefinition.fillPlaceholders(placeholders: List<PlaceholderDefinition>): String {
    return DeepLinkHandler.Pattern(value).replacePlaceholders { placeholderKey ->
        val placeholder = checkNotNull(placeholders.find { it.key == placeholderKey }) {
            "Did not find a placeholder with key $placeholderKey"
        }
        placeholder.exampleValues.first()
    }
}
