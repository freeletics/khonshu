package com.freeletics.khonshu.deeplinks.script

import com.freeletics.khonshu.navigation.deeplinks.DeepLinkDefinitions
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option

internal class ListDeepLinksCli : CliktCommand(name = "list") {
    private val deepLinkDefinitions by requireObject<DeepLinkDefinitions>()

    private val printExamples: Boolean by option("--examples")
        .help("Print deep link examples")
        .flag(default = false)

    override fun run() {
        deepLinkDefinitions.forEachDeepLink { index, key, deepLink, examples ->
            echo("[$index] $key")
            echo("  URL: $deepLink")

            if (printExamples) {
                echo()
                echo("  Examples:")
                examples.forEachIndexed { exampleIndex, it ->
                    echo("  [${index + exampleIndex}]: $it")
                }
                echo()
            }
        }
    }

    override fun help(context: Context): String {
        return "List all supported deep links"
    }
}
