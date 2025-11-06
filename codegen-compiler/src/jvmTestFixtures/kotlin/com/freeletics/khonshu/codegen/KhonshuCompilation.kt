@file:Suppress("DEPRECATION")
@file:OptIn(ExperimentalCompilerApi::class)

package com.freeletics.khonshu.codegen

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import java.io.File
import java.nio.file.Files
import java.util.Collections.emptyList
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

interface KhonshuCompilation {
    fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit)

    fun generatedFileFor(name: String): String

    companion object {
        fun simpleCompilation(
            sources: List<Pair<String, String>>,
            compilerPlugins: List<CompilerPluginRegistrar> = emptyList(),
            warningsAsErrors: Boolean = true,
        ): KhonshuCompilation {
            return SimpleKhonshuCompilation(
                sources = sources,
                compilerPlugins = compilerPlugins,
                warningsAsErrors = warningsAsErrors,
            )
        }

        fun kspCompilation(
            @Language("kotlin") source: String,
            fileName: String = "Test.kt",
            compilerPlugins: List<CompilerPluginRegistrar> = emptyList(),
            symbolProcessors: List<SymbolProcessorProvider> = emptyList(),
            warningsAsErrors: Boolean = true,
        ): KhonshuCompilation {
            return KspKhonshuCompilation(
                sources = listOf(fileName to source),
                compilerPlugins = compilerPlugins,
                symbolProcessors = symbolProcessors,
                warningsAsErrors = warningsAsErrors,
            )
        }
    }
}

private class SimpleKhonshuCompilation(
    sources: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
    warningsAsErrors: Boolean,
) : KhonshuCompilation {
    val compilation = KotlinCompilation().apply {
        configure(sourceFiles = sources, compilerPlugins = compilerPlugins, warningsAsErrors = warningsAsErrors)
    }

    override fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit) {
        return block(compilation.compile())
    }

    override fun generatedFileFor(name: String): String {
        throw UnsupportedOperationException("Simple compilation does not support generating files")
    }
}

private class KspKhonshuCompilation(
    sources: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
    symbolProcessors: List<SymbolProcessorProvider>,
    warningsAsErrors: Boolean,
) : KhonshuCompilation {
    val compilation = KotlinCompilation().apply {
        configure(sourceFiles = sources, compilerPlugins = compilerPlugins, warningsAsErrors = warningsAsErrors)
        useKsp2()
        symbolProcessorProviders = symbolProcessors.toMutableList()
    }

    override fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit) {
        return block(compilation.compile())
    }

    override fun generatedFileFor(name: String): String {
        val path = "${compilation.kspSourcesDir.absolutePath}/kotlin/${name.testFileName()}"
        return File(path).readText()
    }
}

private fun KotlinCompilation.configure(
    sourceFiles: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
    warningsAsErrors: Boolean,
    kotlinLanguageVersion: String? = null,
) {
    compilerPluginRegistrars += compilerPlugins
    jvmTarget = "11"
    languageVersion = kotlinLanguageVersion
    inheritClassPath = true
    messageOutputStream = System.out // see diagnostics in real time
    allWarningsAsErrors = warningsAsErrors
    sources = sourceFiles.map { (fileName, source) ->
        sourceFile(fileName, source)
    }
    kotlincArguments += listOf("-Xskip-prerelease-check")
}

private fun KotlinCompilation.sourceFile(name: String, content: String): SourceFile {
    val path = "${workingDir.absolutePath}/sources/src/jvmMain/kotlin/$name"
    Files.createDirectories(File(path).parentFile!!.toPath())
    return SourceFile.kotlin(path, content)
}

public fun String.testFileName(): String {
    val path = substringBeforeLast("/")
    val name = substringAfterLast("/")
    return "$path/Khonshu$name"
}

public fun simpleSymbolProcessor(block: (Resolver) -> Unit): SymbolProcessorProvider {
    return SymbolProcessorProvider {
        object : SymbolProcessor {
            override fun process(resolver: Resolver): List<KSAnnotated> {
                block(resolver)
                return emptyList()
            }
        }
    }
}
