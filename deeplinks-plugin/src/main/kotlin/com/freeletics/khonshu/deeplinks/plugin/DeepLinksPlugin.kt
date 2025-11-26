package com.freeletics.khonshu.deeplinks.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for deeplinks integration:
 * - appends intent filters to app's manifest for deeplinks from configuration file
 */
public abstract class DeepLinksPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("deepLinks", DeepLinksExtension::class.java)
        extension.deepLinkDefinitionsFile.convention(project.layout.projectDirectory.file("deeplinks.toml"))

        var foundPlugin = false
        project.plugins.withId("com.android.library") {
            foundPlugin = true
            setupDeepLinksManifestConfigurator(project, extension)
        }

        project.plugins.withId("com.android.application") {
            foundPlugin = true
            setupDeepLinksManifestConfigurator(project, extension)
        }

        project.plugins.withId("com.android.kotlin.multiplatform.library") {
            foundPlugin = true
            setupDeepLinksManifestConfigurator(project, extension)
        }

        project.afterEvaluate {
            check(foundPlugin) {
                "No Android Gradle plugin was found. Please apply the 'com.android.library', " +
                    "'com.android.application' or 'com.android.kotlin.multiplatform.library' plugin."
            }
        }
    }

    private fun setupDeepLinksManifestConfigurator(project: Project, extension: DeepLinksExtension) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val name = "${variant.name}DeeplinksManifestConfigurator"
            val manifestUpdater = project.tasks.register(name, DeepLinksManifestConfiguratorTask::class.java) {
                it.deepLinkDefinitionsFile.set(extension.deepLinkDefinitionsFile)
            }
            variant.artifacts.use(manifestUpdater).wiredWithFiles(
                DeepLinksManifestConfiguratorTask::mergedManifest,
                DeepLinksManifestConfiguratorTask::updatedManifest,
            ).toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }
}
