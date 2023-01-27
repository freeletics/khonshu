package com.freeletics.mad.whetstone.codegen

import androidx.compose.compiler.plugins.kotlin.ComposeComponentRegistrar
import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.google.common.truth.Truth.assertThat
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.testing.AnvilCompilation
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import java.io.File
import java.nio.file.Files
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

public fun test(data: ComposeScreenData, fileName: String, source: String, expected: String) {
    val actual = FileGenerator().generate(data).toString()
    assertThat(actual).isEqualTo(expected)
    compile(fileName, source, actual)
    compileWithAnvil(fileName, source, actual)
}

public fun test(data: ComposeFragmentData, fileName: String, source: String, expected: String) {
    val actual = FileGenerator().generate(data).toString()
    assertThat(actual).isEqualTo(expected)
    compile(fileName, source, actual)
    compileWithAnvil(fileName, source, actual)
}

public fun test(data: RendererFragmentData, fileName: String, source: String, expected: String) {
    val actual = FileGenerator().generate(data).toString()
    assertThat(actual).isEqualTo(expected)
    compile(fileName, source, actual)
    compileWithAnvil(fileName, source, actual)
}

@OptIn(ExperimentalCompilerApi::class)
private fun compile(fileName: String, source: String, output: String) {
    val compilation = KotlinCompilation().apply {
        configure()

        sources = listOf(
            sourceFile(fileName, source),
            sourceFile(fileName.testFileName(), output),
        )
    }

    assertThat(compilation.compile().exitCode).isEqualTo(ExitCode.OK)
}

@OptIn(ExperimentalCompilerApi::class, ExperimentalAnvilApi::class)
private fun compileWithAnvil(fileName: String, source: String, output: String) {
    val compilation = AnvilCompilation().apply {
        configureAnvil()
        kotlinCompilation.configure()
        useIR(true)

        kotlinCompilation.sources = listOf(
            kotlinCompilation.sourceFile(fileName, source),
        )
    }

    assertThat(compilation.compile().exitCode).isEqualTo(ExitCode.OK)
    assertThat(compilation.kotlinCompilation.generatedFile(fileName)).isEqualTo(output)
}

@OptIn(ExperimentalCompilerApi::class)
private fun KotlinCompilation.sourceFile(name: String, content: String): SourceFile {
    val path = "${workingDir.absolutePath}/sources/src/main/kotlin/$name"
    Files.createDirectories(File(path).parentFile!!.toPath())
    return SourceFile.kotlin(path, content)
}

@OptIn(ExperimentalCompilerApi::class)
private fun KotlinCompilation.generatedFile(name: String): String {
    val path = "${workingDir.absolutePath}/build/anvil/${name.testFileName()}"
    return File(path).readText()
}

private fun String.testFileName(): String {
    val path = substringBeforeLast("/")
    val name = substringAfterLast("/")
    return "$path/Whetstone$name"
}

@OptIn(ExperimentalCompilerApi::class)
private fun KotlinCompilation.configure() {
    @Suppress("DEPRECATION") // can be changed once compose uses ComponentPluginRegistrar
    componentRegistrars = componentRegistrars + listOf(ComposeComponentRegistrar())
    kotlincArguments = kotlincArguments + listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.7.22"
    )
    jvmTarget = "11"
    inheritClassPath = true
    messageOutputStream = System.out // see diagnostics in real time
}
