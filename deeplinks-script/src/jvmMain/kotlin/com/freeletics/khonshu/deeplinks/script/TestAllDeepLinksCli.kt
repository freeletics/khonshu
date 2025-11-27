
package com.freeletics.khonshu.deeplinks.script

import com.freeletics.khonshu.navigation.deeplinks.DeepLinkDefinitions
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.YesNoPrompt
import dadb.Dadb

internal class TestAllDeepLinksCli : CliktCommand(name = "test-all") {
    private val deepLinkDefinitions by requireObject<DeepLinkDefinitions>()

    override fun run() {
        val dadb = Dadb.discover() ?: throw UsageError("No device connected")

        deepLinkDefinitions.forEachDeepLink { _, _, _, examples ->
            examples.forEach {
                launchDeeplinkIntent(dadb, it)
                if (!shouldContinue()) {
                    return
                }
            }
        }
    }

    private fun shouldContinue(): Boolean {
        return YesNoPrompt("Testing finished. Continue? (default y)", Terminal(), default = true).ask() == true
    }

    override fun help(context: Context): String {
        return "Test all supported deep links by launching them one by one on a connected Android device."
    }
}
