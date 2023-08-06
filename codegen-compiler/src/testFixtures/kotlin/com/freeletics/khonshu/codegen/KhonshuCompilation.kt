package com.freeletics.khonshu.codegen

import com.squareup.anvil.compiler.api.CodeGenerator
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.testing.simpleCodeGenerator as anvilSimpleCodeGenerator
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import java.io.File
import java.nio.file.Files
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar

interface KhonshuCompilation {
    fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit)
    fun generatedFileFor(name: String): String

    companion object {
        fun simpleCompilation(
            sources: List<Pair<String, String>>,
            compilerPlugins: List<CompilerPluginRegistrar> = emptyList(),
        ): KhonshuCompilation {
            return SimpleKhonshuCompilation(
                sources = sources,
                compilerPlugins = compilerPlugins,
            )
        }

        fun anvilCompilation(
            @Language("kotlin") source: String,
            fileName: String = "Test.kt",
            compilerPlugins: List<CompilerPluginRegistrar> = emptyList(),
            codeGenerators: List<CodeGenerator> = emptyList(),
        ): KhonshuCompilation {
            return AnvilKhonshuCompilation(
                sources = listOf(fileName to source),
                compilerPlugins = compilerPlugins,
                codeGenerators = codeGenerators,
            )
        }
    }
}

private class SimpleKhonshuCompilation(
    sources: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
) : KhonshuCompilation {
    val compilation = KotlinCompilation().apply {
        configure(sources, compilerPlugins)
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
    codeGenerators: List<CodeGenerator>,
) : KhonshuCompilation {
    val compilation = AnvilCompilation().apply {
        configureAnvil(codeGenerators = codeGenerators)
        kotlinCompilation.configure(sources, compilerPlugins)
    }

    override fun compile(block: KhonshuCompilation.(JvmCompilationResult) -> Unit) {
        return block(compilation.kotlinCompilation.compile())
    }

    override fun generatedFileFor(name: String): String {
        val path = "${compilation.kotlinCompilation.workingDir.absolutePath}/build/anvil/${name.testFileName()}"
        return File(path).readText()
    }
}

private fun KotlinCompilation.configure(
    sourceFiles: List<Pair<String, String>>,
    compilerPlugins: List<CompilerPluginRegistrar>,
) {
    compilerPluginRegistrars += compilerPlugins
    jvmTarget = "11"
    inheritClassPath = true
    messageOutputStream = System.out // see diagnostics in real time
    allWarningsAsErrors = true
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
