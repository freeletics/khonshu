package com.freeletics.khonshu.deeplinks.plugin

import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import java.io.File

private const val PLACEHOLDER = "<!-- DEEPLINK INTENT FILTERS -->"

internal fun configure(
    configurationFile: File,
    inputManifestFile: File,
    outputManifestFile: File,
) {
    val manifest = inputManifestFile.readLines().toMutableList()
    val placeholderIndex = manifest.indexOfFirst { it.contains(PLACEHOLDER) }
    check(placeholderIndex >= 0) {
        "Did not find $PLACEHOLDER in given manifest ${inputManifestFile.absolutePath}"
    }

    val definitions = DeepLinkDefinitions.decodeFromString(configurationFile.readText())
    val indentation = manifest[placeholderIndex].takeWhile { it == ' ' }
    manifest[placeholderIndex] = intentFiltersFromConfig(definitions, indentation)

    outputManifestFile.writeText(manifest.joinToString(separator = "\n"))
}

private fun intentFiltersFromConfig(definitions: DeepLinkDefinitions, indentation: String): String {
    val builder = IntentFilterBuilder(indentation)

    val deepLinksWithGlobalPrefixes = definitions.deepLinks.values.filter { it.prefixes.isEmpty() }
    if (deepLinksWithGlobalPrefixes.isNotEmpty()) {
        check(definitions.prefixes.isNotEmpty()) {
            "Configuration contains deep links without a prefix but has no global prefixes"
        }

        definitions.prefixes.forEach { prefix ->
            builder.appendIntentFilter(prefix, deepLinksWithGlobalPrefixes)
        }
    }

    definitions.deepLinks.values.filter { it.prefixes.isNotEmpty() }.forEach {
        it.prefixes.forEach { prefix ->
            builder.appendIntentFilter(prefix, listOf(it))
        }
    }

    return builder.toString()
}

private fun IntentFilterBuilder.appendIntentFilter(prefix: PrefixDefinition, deepLinks: List<DeepLinkDefinition>) {
    start(prefix.autoVerified)
    appendAction("android.intent.action.VIEW")
    appendCategory("android.intent.category.DEFAULT")
    appendCategory("android.intent.category.BROWSABLE")

    deepLinks.forEach { deepLink ->
        deepLink.patterns.forEach { pattern ->
            appendData(prefix.scheme, prefix.host, pattern)
        }
    }
    end()
}

private class IntentFilterBuilder(
    private val indentation: String,
) {
    private val builder = StringBuilder()

    fun start(autoVerify: Boolean) {
        builder.appendLine("$indentation<intent-filter android:autoVerify=\"${autoVerify}\">")
    }

    fun appendAction(action: String) {
        builder.appendLine("$indentation    <action android:name=\"${action}\" />")
    }

    fun appendCategory(category: String) {
        builder.appendLine("$indentation    <category android:name=\"${category}\" />")
    }

    @OptIn(InternalNavigationApi::class)
    fun appendData(scheme: String, host: String, pattern: PatternDefinition) {
        val pathPattern = pattern.replacePlaceholders { ".*" }
        builder.appendLine("$indentation    <data")
        builder.appendLine("$indentation        android:scheme=\"${scheme}\"")
        builder.appendLine("$indentation        android:host=\"${host}\"")
        builder.appendLine("$indentation        android:pathPattern=\"/${pathPattern}\"")
        builder.appendLine("$indentation        />")
    }

    fun end() {
        builder.appendLine("$indentation</intent-filter>")
    }

    override fun toString(): String {
        return builder.toString().trimEnd()
    }
}
