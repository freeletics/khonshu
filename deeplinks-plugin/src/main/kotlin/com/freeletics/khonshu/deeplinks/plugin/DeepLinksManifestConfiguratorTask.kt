package com.freeletics.khonshu.deeplinks.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class DeepLinksManifestConfiguratorTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    public abstract val deepLinkDefinitionsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
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
