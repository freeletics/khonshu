package com.freeletics.khonshu.codegen.codegen

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.KhonshuCompilation.Companion.anvilCompilation
import com.freeletics.khonshu.codegen.KhonshuCompilation.Companion.kspCompilation
import com.freeletics.khonshu.codegen.KhonshuCompilation.Companion.simpleCompilation
import com.freeletics.khonshu.codegen.KhonshuSymbolProcessor.KhonshuSymbolProcessorProvider
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.testFileName
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode

internal fun test(data: BaseData, fileName: String, source: String, expectedCode: String) {
    compile(fileName = fileName, source = source, data = data, expectedCode = expectedCode)
    if (data !is NavHostActivityData) {
        compileWithAnvil(fileName = fileName, source = source, expectedCode = expectedCode)
        compileWithKsp(fileName = fileName, source = source, expectedCode = expectedCode)
    }
}

private fun compile(fileName: String, source: String, data: BaseData, expectedCode: String) {
    val generatedCode = FileGenerator().generate(data).toString()

    assertThat(generatedCode).isEqualTo(expectedCode)

    simpleCompilation(
        sources = listOf(
            fileName to source,
            fileName.testFileName() to generatedCode,
        ),
        compilerPlugins = listOf(ComposePluginRegistrar()),
    ).compile {
        assertThat(it.exitCode).isEqualTo(ExitCode.OK)
    }
}

private fun compileWithAnvil(fileName: String, source: String, expectedCode: String) {
    anvilCompilation(
        source = source,
        fileName = fileName,
        compilerPlugins = listOf(ComposePluginRegistrar()),
    ).compile {
        assertThat(it.exitCode).isEqualTo(ExitCode.OK)
        assertThat(generatedFileFor(fileName)).isEqualTo(expectedCode)
    }
}

private fun compileWithKsp(fileName: String, source: String, expectedCode: String) {
    kspCompilation(
        source = source,
        fileName = fileName,
        compilerPlugins = listOf(ComposePluginRegistrar()),
        symbolProcessors = listOf(KhonshuSymbolProcessorProvider()),
    ).compile {
        assertThat(it.exitCode).isEqualTo(ExitCode.OK)
        assertThat(generatedFileFor(fileName)).isEqualTo(expectedCode)
    }
}
