package com.freeletics.khonshu.deeplinks.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project

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

        project.plugins.withId("com.android.library") {
            setupDeeplinksManifestConfigurator(project, configurationFile)
        }

        project.plugins.withId("com.android.application") {
            setupDeeplinksManifestConfigurator(project, configurationFile)
        }
    }

    private fun setupDeeplinksManifestConfigurator(project: Project, configurationFile: File) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val manifestUpdater = project.tasks.register(
                variant.name + "DeeplinksManifestConfigurator",
                DeeplinksManifestConfiguratorTask::class.java,
            ) {
                it.deeplinksConfigurationFile.set(
                    configurationFile,
                )
            }
            variant.artifacts.use(manifestUpdater).wiredWithFiles(
                DeeplinksManifestConfiguratorTask::mergedManifest,
                DeeplinksManifestConfiguratorTask::updatedManifest,
            ).toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }
}
