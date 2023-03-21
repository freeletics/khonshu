package com.freeletics.mad.deeplinks.plugin

import com.google.common.truth.Truth
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DeeplinksManifestConfiguratorTest {

    @get:Rule
    var tmpFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `simple text replace test`() = test {
        Truth.assertThat(runTest()).isEqualTo(
            """
                pre content
                deeplinks go here
                post content
            """.trimIndent()
        )
    }

    private fun test(
        configurationContent: String = SAMPLE_CONFIG,
        inputManifestContent: String = INPUT_MANIFEST_CONTENT.trimIndent(),
        testBlock: TestScope.() -> Unit
    ) {
        val testScope = TestScope(tmpFolder, configurationContent, inputManifestContent)
        testBlock.invoke(testScope)
    }

    companion object {
        const val SAMPLE_CONFIG = "deeplinks go here"
        const val INPUT_MANIFEST_CONTENT =
            """
                pre content
                <!-- DEEPLINK INTENT FILTERS -->
                post content
            """
    }
}

private class TestScope(
    temporaryFolder: TemporaryFolder,
    configurationContent: String,
    inputManifestContent: String,
) {

    private val outputManifestFile: File
    private val configurator: DeeplinksManifestConfigurator

    init {
        val configFile = temporaryFolder.newFile("config")
        configFile.writeText(configurationContent)

        val inputManifestFile = temporaryFolder.newFile("inputManifest")
        inputManifestFile.writeText(inputManifestContent)

        outputManifestFile = temporaryFolder.newFile("outputManifest")
        configurator = DeeplinksManifestConfigurator(
            configFile, inputManifestFile, outputManifestFile
        )
    }

    fun runTest(): String {
        configurator.configure()
        return outputManifestFile.readText()
    }
}
