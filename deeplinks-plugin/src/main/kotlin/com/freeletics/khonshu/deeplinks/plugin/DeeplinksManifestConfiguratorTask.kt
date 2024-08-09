package com.freeletics.khonshu.deeplinks.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class DeeplinksManifestConfiguratorTask : DefaultTask() {
    @get:InputFile
    public abstract val deepLinkDefinitionsFile: RegularFileProperty

    @get:InputFile
    public abstract val mergedManifest: RegularFileProperty

    @get:OutputFile
    public abstract val updatedManifest: RegularFileProperty

    @TaskAction
    public fun taskAction() {
        val configurationFile = deepLinkDefinitionsFile.get().asFile
        val inputManifest = mergedManifest.get().asFile
        val outputManifest = updatedManifest.get().asFile

        configure(configurationFile, inputManifest, outputManifest)
    }
}
