@file:Suppress("DEPRECATION")

package com.freeletics.khonshu.codegen

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.testing.simpleCodeGenerator as anvilSimpleCodeGenerator
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.io.File
import java.nio.file.Files
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar

interface KhonshuCompilation {
    fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit)
    fun generatedFileFor(name: String): String

    companion object {
        fun simpleCompilation(
            sources: List<Pair<String, String>>,
            compilerPlugins: List<CompilerPluginRegistrar> = emptyList(),
            legacyCompilerPlugins: List<ComponentRegistrar> = emptyList(),
            warningsAsErrors: Boolean = true,
        ): KhonshuCompilation {
            return SimpleKhonshuCompilation(
                sources = sources,
                compilerPlugins = compilerPlugins,
                legacyCompilerPlugins = legacyCompilerPlugins,
                warningsAsErrors = warningsAsErrors,
            )
        }

        fun anvilCompilation(
            @Language("kotlin") source: String,
            fileName: String = "Test.kt",
            compilerPlugins: List<CompilerPluginRegistrar> = emptyList(),
            legacyCompilerPlugins: List<ComponentRegistrar> = emptyList(),
            codeGenerators: List<CodeGenerator> = emptyList(),
            warningsAsErrors: Boolean = true,
        ): KhonshuCompilation {
            return AnvilKhonshuCompilation(
                sources = listOf(fileName to source),
                compilerPlugins = compilerPlugins,
                legacyCompilerPlugins = legacyCompilerPlugins,
                codeGenerators = codeGenerators,
                warningsAsErrors = warningsAsErrors,
            )
        }

        fun kspCompilation(
            @Language("kotlin") source: String,
            fileName: String = "Test.kt",
            compilerPlugins: List<CompilerPluginRegistrar> = emptyList(),
            legacyCompilerPlugins: List<ComponentRegistrar> = emptyList(),
            symbolProcessors: List<SymbolProcessorProvider> = emptyList(),
            warningsAsErrors: Boolean = true,
        ): KhonshuCompilation {
            return KspKhonshuCompilation(
                sources = listOf(fileName to source),
                compilerPlugins = compilerPlugins,
                legacyCompilerPlugins = legacyCompilerPlugins,
                symbolProcessors = symbolProcessors,
                warningsAsErrors = warningsAsErrors,
            )
        }
    }
}

private class SimpleKhonshuCompilation(
    sources: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
    legacyCompilerPlugins: List<ComponentRegistrar>,
    warningsAsErrors: Boolean,
) : KhonshuCompilation {
    val compilation = KotlinCompilation().apply {
        configure(sources, compilerPlugins, legacyCompilerPlugins, warningsAsErrors)
    }

    override fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit) {
        return block(compilation.compile())
    }

    override fun generatedFileFor(name: String): String {
        throw UnsupportedOperationException("Simple compilation does not support generating files")
    }
}

private class AnvilKhonshuCompilation(
    sources: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
    legacyCompilerPlugins: List<ComponentRegistrar>,
    codeGenerators: List<CodeGenerator>,
    warningsAsErrors: Boolean,
) : KhonshuCompilation {
    val compilation = AnvilCompilation().apply {
        configureAnvil(codeGenerators = codeGenerators)
        kotlinCompilation.configure(sources, compilerPlugins, legacyCompilerPlugins, warningsAsErrors)
    }

    override fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit) {
        return block(compilation.kotlinCompilation.compile())
    }

    override fun generatedFileFor(name: String): String {
        val path = "${compilation.kotlinCompilation.workingDir.absolutePath}/build/anvil/${name.testFileName()}"
        return File(path).readText()
    }
}

private class KspKhonshuCompilation(
    sources: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
    legacyCompilerPlugins: List<ComponentRegistrar>,
    symbolProcessors: List<SymbolProcessorProvider>,
    warningsAsErrors: Boolean,
) : KhonshuCompilation {
    val compilation = KotlinCompilation().apply {
        symbolProcessorProviders = symbolProcessors
        configure(sources, compilerPlugins, legacyCompilerPlugins, warningsAsErrors)
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
    legacyCompilerPlugins: List<ComponentRegistrar>,
    warningsAsErrors: Boolean,
) {
    componentRegistrars += legacyCompilerPlugins
    compilerPluginRegistrars += compilerPlugins
    jvmTarget = "11"
    inheritClassPath = true
    messageOutputStream = System.out // see diagnostics in real time
    allWarningsAsErrors = warningsAsErrors
    sources = sourceFiles.map { (fileName, source) ->
        sourceFile(fileName, source)
    }
}

private fun KotlinCompilation.sourceFile(name: String, content: String): SourceFile {
    val path = "${workingDir.absolutePath}/sources/src/main/kotlin/$name"
    Files.createDirectories(File(path).parentFile!!.toPath())
    return SourceFile.kotlin(path, content)
}

public fun String.testFileName(): String {
    val path = substringBeforeLast("/")
    val name = substringAfterLast("/")
    return "$path/Khonshu$name"
}

public fun simpleCodeGenerator(block: (ClassReference) -> Unit): CodeGenerator {
    return anvilSimpleCodeGenerator {
        block(it)
        null
    }
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
