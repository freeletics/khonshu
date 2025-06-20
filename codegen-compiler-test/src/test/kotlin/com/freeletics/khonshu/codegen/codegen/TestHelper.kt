@file:OptIn(ExperimentalCompilerApi::class)

package com.freeletics.khonshu.codegen.codegen

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.KhonshuCompilation.Companion.kspCompilation
import com.freeletics.khonshu.codegen.KhonshuCompilation.Companion.simpleCompilation
import com.freeletics.khonshu.codegen.KhonshuSymbolProcessor.KhonshuSymbolProcessorProvider
import com.freeletics.khonshu.codegen.testFileName
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

internal fun test(
    data: BaseData,
    fileName: String,
    source: String,
    expectedCode: String,
    warningsAsErrors: Boolean = true,
) {
    compile(fileName = fileName, source = source, data = data, expectedCode = expectedCode, warningsAsErrors)
    compileWithKsp(fileName = fileName, source = source, expectedCode = expectedCode, warningsAsErrors)
}

private fun compile(fileName: String, source: String, data: BaseData, expectedCode: String, warningsAsErrors: Boolean) {
    val generatedCode = FileGenerator().generate(data).toString()

    assertThat(generatedCode).isEqualTo(expectedCode)

    simpleCompilation(
        sources = listOf(
            fileName to source,
            fileName.testFileName() to generatedCode,
        ),
        compilerPlugins = listOf(ComposePluginRegistrar()),
        warningsAsErrors = warningsAsErrors,
    ).compile {
        assertThat(it.exitCode).isEqualTo(ExitCode.OK)
    }
}

private fun compileWithKsp(
    fileName: String,
    source: String,
    expectedCode: String,
    warningsAsErrors: Boolean,
) {
    kspCompilation(
        source = source,
        fileName = fileName,
        compilerPlugins = listOf(ComposePluginRegistrar()),
        symbolProcessors = listOf(KhonshuSymbolProcessorProvider()),
        warningsAsErrors = warningsAsErrors,
    ).compile {
        assertThat(it.exitCode).isEqualTo(ExitCode.OK)
        assertThat(generatedFileFor(fileName)).isEqualTo(expectedCode)
    }
}
