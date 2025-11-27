package com.freeletics.khonshu.deeplinks.script

import com.freeletics.khonshu.navigation.deeplinks.DeepLinkDefinitions
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.obj
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.readText

public class DeepLinksCli : CliktCommand() {
    private val deepLinkDefinitionsFile: Path by option("--deep-link-definitions-file", "--definitions-file", "-f")
        .help("The root directory of the project, used as starting point to search files and to find editorconfig.")
        .path(mustExist = true, canBeFile = true, canBeDir = false, mustBeReadable = true)
        .default(Paths.get("src/test/resources/deeplinks.toml").absolute())

    init {
        subcommands(ListDeepLinksCli(), TestDeeplinksCli(), TestAllDeepLinksCli())
    }

    override fun run() {
        val decoded = DeepLinkDefinitions.decodeFromString(deepLinkDefinitionsFile.readText())
        currentContext.obj = DeepLinkDefinitions(
            decoded.prefixes,
            decoded.placeholders,
            decoded.deepLinks.toSortedMap(),
        )
    }

    override fun help(context: Context): String {
        return "DeepLink related commands"
    }
}
