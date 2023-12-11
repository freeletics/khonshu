// copy from Anvil until they release a version that uses compile testing 0.3.0
package com.freeletics.khonshu.codegen

import com.google.auto.value.processor.AutoAnnotationProcessor
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.AnvilCommandLineProcessor
import com.squareup.anvil.compiler.AnvilComponentRegistrar
import com.squareup.anvil.compiler.api.CodeGenerator
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import dagger.internal.codegen.ComponentProcessor
import java.io.File
import org.jetbrains.kotlin.config.JvmTarget

/**
 * A simple API over a [KotlinCompilation] with extra configuration support for Anvil.
 */
@ExperimentalAnvilApi
internal class AnvilCompilation internal constructor(
    val kotlinCompilation: KotlinCompilation,
) {

    private var anvilConfigured = false

    /** Configures this the Anvil behavior of this compilation. */
    @ExperimentalAnvilApi
    fun configureAnvil(
        enableDaggerAnnotationProcessor: Boolean = false,
        generateDaggerFactories: Boolean = false,
        generateDaggerFactoriesOnly: Boolean = false,
        disableComponentMerging: Boolean = false,
        enableExperimentalAnvilApis: Boolean = true,
        codeGenerators: List<CodeGenerator> = emptyList(),
        enableAnvil: Boolean = true,
    ): AnvilCompilation = apply {
        check(!anvilConfigured) { "Anvil should not be configured twice." }

        anvilConfigured = true

        if (!enableAnvil) return@apply

        kotlinCompilation.apply {
            // Deprecation tracked in https://github.com/square/anvil/issues/672
            @Suppress("DEPRECATION")
            componentRegistrars = listOf(
                AnvilComponentRegistrar().also { it.addCodeGenerators(codeGenerators) },
            )
            if (enableDaggerAnnotationProcessor) {
                annotationProcessors = listOf(ComponentProcessor(), AutoAnnotationProcessor())
            }

            val anvilCommandLineProcessor = AnvilCommandLineProcessor()
            commandLineProcessors = listOf(anvilCommandLineProcessor)

            pluginOptions = listOf(
                PluginOption(
                    pluginId = anvilCommandLineProcessor.pluginId,
                    optionName = "src-gen-dir",
                    optionValue = File(workingDir, "build/anvil").absolutePath,
                ),
                PluginOption(
                    pluginId = anvilCommandLineProcessor.pluginId,
                    optionName = "generate-dagger-factories",
                    optionValue = generateDaggerFactories.toString(),
                ),
                PluginOption(
                    pluginId = anvilCommandLineProcessor.pluginId,
                    optionName = "generate-dagger-factories-only",
                    optionValue = generateDaggerFactoriesOnly.toString(),
                ),
                PluginOption(
                    pluginId = anvilCommandLineProcessor.pluginId,
                    optionName = "disable-component-merging",
                    optionValue = disableComponentMerging.toString(),
                ),
            )

            if (enableExperimentalAnvilApis) {
                kotlincArguments += listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi",
                )
            }
        }
    }

    internal companion object {
        operator fun invoke(): AnvilCompilation {
            return AnvilCompilation(
                KotlinCompilation().apply {
                    // Sensible default behaviors
                    inheritClassPath = true
                    jvmTarget = JvmTarget.JVM_1_8.description
                    verbose = false
                },
            )
        }
    }
}
