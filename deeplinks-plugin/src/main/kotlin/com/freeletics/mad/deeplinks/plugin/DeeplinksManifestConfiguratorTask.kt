package com.freeletics.mad.deeplinks.plugin

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class DeeplinksManifestConfiguratorTask: DefaultTask() {

    @get:InputFile
    abstract val deeplinksConfigurationFile: RegularFileProperty

    @get:InputFile
    abstract val mergedManifest: RegularFileProperty

    @get:OutputFile
    abstract val updatedManifest: RegularFileProperty

    @TaskAction
    fun taskAction() {
        val configurationFile = deeplinksConfigurationFile.get().asFile
        val inputManifest = mergedManifest.get().asFile
        val outputManifest = updatedManifest.get().asFile

        DeeplinksManifestConfigurator(configurationFile, inputManifest, outputManifest).configure()
    }
}

internal class DeeplinksManifestConfigurator(
    private val configurationFile: File,
    private val inputManifestFile: File,
    private val outputManifestFile: File,
) {
    fun configure() {
        val configuration = configurationFile.readText()

        var manifest = inputManifestFile.readText()
        manifest = manifest.replace(PLACEHOLDER, configuration)

        outputManifestFile.writeText(manifest)
    }

    companion object {
        const val PLACEHOLDER = "<!-- DEEPLINK INTENT FILTERS -->"
    }
}
