package com.freeletics.mad.deeplinks.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException

/**
 * Plugin for deeplinks integration:
 * - appends intent filters to app's manifest for deeplinks from configuration file
 */
abstract class DeeplinksPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val configurationFile = File(project.projectDir, "deeplinks.toml")
        if (!configurationFile.exists()) {
            // configuration file doesn't exist -> skipping
            return
        }
        val androidComponents = try {
            project.extensions.getByType(AndroidComponentsExtension::class.java)
        } catch (e: UnknownDomainObjectException) {
            // can't find android extension -> probably project doesn't have Android plugin applied
            return
        }
        androidComponents.onVariants { variant ->
            val manifestUpdater =
                project.tasks.register(
                    variant.name + "DeeplinksManifestConfigurator",
                    DeeplinksManifestConfiguratorTask::class.java
                ) {
                    it.deeplinksConfigurationFile.set(
                        configurationFile
                    )
                }
            variant.artifacts.use(manifestUpdater)
                .wiredWithFiles(
                    DeeplinksManifestConfiguratorTask::mergedManifest,
                    DeeplinksManifestConfiguratorTask::updatedManifest
                )
                .toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }
}