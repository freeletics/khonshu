package com.freeletics.khonshu.deeplinks.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for deeplinks integration:
 * - appends intent filters to app's manifest for deeplinks from configuration file
 */
public abstract class DeeplinksPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("deepLinks", DeepLinksExtension::class.java)
        extension.deepLinkDefinitionsFile.convention(project.layout.projectDirectory.file("deeplinks.toml"))

        project.plugins.withId("com.android.library") {
            setupDeeplinksManifestConfigurator(project, extension)
        }

        project.plugins.withId("com.android.application") {
            setupDeeplinksManifestConfigurator(project, extension)
        }
    }

    private fun setupDeeplinksManifestConfigurator(project: Project, extension: DeepLinksExtension) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val name = "${variant.name}DeeplinksManifestConfigurator"
            val manifestUpdater = project.tasks.register(name, DeeplinksManifestConfiguratorTask::class.java) {
                it.deepLinkDefinitionsFile.set(extension.deepLinkDefinitionsFile)
            }
            variant.artifacts.use(manifestUpdater).wiredWithFiles(
                DeeplinksManifestConfiguratorTask::mergedManifest,
                DeeplinksManifestConfiguratorTask::updatedManifest,
            ).toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }
}
