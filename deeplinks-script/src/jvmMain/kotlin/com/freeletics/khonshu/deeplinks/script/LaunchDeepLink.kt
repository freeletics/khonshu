package com.freeletics.khonshu.deeplinks.script

import com.eygraber.uri.Uri
import com.github.ajalt.clikt.core.CliktCommand
import dadb.Dadb

internal fun CliktCommand.launchDeeplinkIntent(dadb: Dadb, deeplink: String) {
    echo("Testing deeplink: $deeplink")

    val uri = Uri.parse(deeplink)
    val encodedUri = Uri.Builder()
        .scheme(uri.scheme)
        .authority(uri.authority)
        .apply {
            uri.pathSegments.forEach {
                appendPath(it)
            }
            uri.getQueryParameterNames().forEach { key ->
                appendQueryParameter(key, uri.getQueryParameter(key))
            }
        }
        .build()
        .toString()

    val response = dadb.shell("am start -W -a android.intent.action.VIEW -d \"$encodedUri\"")
    assert(response.exitCode == 0)
}
