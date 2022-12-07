package com.freeletics.mad.whetstone.codegen

import androidx.compose.compiler.plugins.kotlin.ComposeComponentRegistrar
import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile

public fun test(data: ComposeScreenData, expected: String) {
    val actual = FileGenerator().generate(data).toString()
    assertThat(actual).isEqualTo(expected)
    compile(actual)
}

public fun test(data: ComposeFragmentData, expected: String) {
    val actual = FileGenerator().generate(data).toString()
    assertThat(actual).isEqualTo(expected)
    compile(actual)
}

public fun test(data: RendererFragmentData, expected: String) {
    val actual = FileGenerator().generate(data).toString()
    assertThat(actual).isEqualTo(expected)
    compile(actual)
}

private fun compile(source: String) {
    val result = KotlinCompilation().apply {
        sources = listOf(SourceFile.kotlin("WhetstoneTest.kt", source))
        compilerPlugins = listOf(ComposeComponentRegistrar())
        kotlincArguments = listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=1.7.22"
        )
        jvmTarget = "11"
        inheritClassPath = true
        messageOutputStream = System.out // see diagnostics in real time
    }.compile()
    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
}
