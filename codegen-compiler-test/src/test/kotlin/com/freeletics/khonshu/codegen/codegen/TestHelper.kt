package com.freeletics.khonshu.codegen.codegen

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.freeletics.khonshu.codegen.AnvilCompilation
import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.RendererFragmentData
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import java.io.File
import java.nio.file.Files

public fun test(data: BaseData, fileName: String, source: String, expectedCode: String) {
    compile(fileName = fileName, source = source, data = data, expectedCode = expectedCode)
    compileWithAnvil(fileName = fileName, source = source, expectedCode = expectedCode)
}

private fun compile(fileName: String, source: String, data: BaseData, expectedCode: String) {
    val generatedCode = when (data) {
        is ComposeFragmentData -> FileGenerator().generate(data).toString()
        is ComposeScreenData -> FileGenerator().generate(data).toString()
        is RendererFragmentData -> FileGenerator().generate(data).toString()
    }

    val compilation = KotlinCompilation().apply {
        configure()

        sources = listOf(
            sourceFile(fileName, source),
            sourceFile(fileName.testFileName(), generatedCode),
        )
    }

    assertThat(compilation.compile().exitCode).isEqualTo(ExitCode.OK)
    assertThat(generatedCode).isEqualTo(expectedCode)
}

private fun compileWithAnvil(fileName: String, source: String, expectedCode: String) {
    val compilation = AnvilCompilation().apply {
        configureAnvil()
        kotlinCompilation.configure()
        kotlinCompilation.sources = listOf(
            kotlinCompilation.sourceFile(fileName, source),
        )
    }

    assertThat(compilation.compile().exitCode).isEqualTo(ExitCode.OK)
    assertThat(compilation.kotlinCompilation.generatedFile(fileName)).isEqualTo(expectedCode)
}

private fun KotlinCompilation.sourceFile(name: String, content: String): SourceFile {
    val path = "${workingDir.absolutePath}/sources/src/main/kotlin/$name"
    Files.createDirectories(File(path).parentFile!!.toPath())
    return SourceFile.kotlin(path, content)
}

private fun KotlinCompilation.generatedFile(name: String): String {
    val path = "${workingDir.absolutePath}/build/anvil/${name.testFileName()}"
    return File(path).readText()
}

private fun String.testFileName(): String {
    val path = substringBeforeLast("/")
    val name = substringAfterLast("/")
    return "$path/Khonshu$name"
}

private fun KotlinCompilation.configure() {
    compilerPluginRegistrars = compilerPluginRegistrars + listOf(ComposePluginRegistrar())
    kotlincArguments = kotlincArguments + listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.7.22",
    )
    jvmTarget = "11"
    inheritClassPath = true
    messageOutputStream = System.out // see diagnostics in real time
    allWarningsAsErrors = true
}
