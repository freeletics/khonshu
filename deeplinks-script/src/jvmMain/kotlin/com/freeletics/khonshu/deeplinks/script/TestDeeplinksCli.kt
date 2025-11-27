
package com.freeletics.khonshu.deeplinks.script

import com.eygraber.uri.Uri
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkDefinitions
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import dadb.Dadb

internal class TestDeeplinksCli : CliktCommand(name = "test") {
    private val deepLinkDefinitions by requireObject<DeepLinkDefinitions>()

    private val deeplinkUrlOrNumber: String by argument()
        .help(
            "Deep link URL or the number of deep link in the list. The number can be obtained from the " +
                "`list --examples` command.",
        )

    override fun run() {
        val dadb = Dadb.discover() ?: throw UsageError("No device connected")

        when {
            deeplinkUrlOrNumber.isNumber() -> {
                val requested = deeplinkUrlOrNumber.toInt()
                deepLinkDefinitions.forEachDeepLink { index, _, _, examples ->
                    if (requested in index..<(index + examples.size)) {
                        launchDeeplinkIntent(dadb, examples[requested - index])
                        return
                    }
                }
                throw UsageError("Invalid deeplink index $requested")
            }
            deeplinkUrlOrNumber.isUri() -> launchDeeplinkIntent(dadb, deeplinkUrlOrNumber)
            else -> throw UsageError("Can't parse deeplink '$deeplinkUrlOrNumber'")
        }
    }

    private fun String.isUri(): Boolean = try {
        val uri = Uri.parse(this)
        uri.scheme != null && uri.host != null
    } catch (_: IllegalArgumentException) {
        false
    }

    private fun String.isNumber(): Boolean = try {
        @Suppress("CheckResult")
        Integer.parseInt(this)
        true
    } catch (_: NumberFormatException) {
        false
    }

    override fun help(context: Context): String {
        return "Launch a deep link on a connected Android device."
    }
}
