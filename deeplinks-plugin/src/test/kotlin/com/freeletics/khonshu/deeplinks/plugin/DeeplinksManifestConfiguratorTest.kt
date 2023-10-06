package com.freeletics.khonshu.deeplinks.plugin

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DeeplinksManifestConfiguratorTest {

    @get:Rule
    var tmpFolder: TemporaryFolder = TemporaryFolder()

    private val tomlHeader = """
        [[prefixes]]
        scheme = "https"
        host = "www.example.com"
        autoVerified = true

        [[prefixes]]
        scheme = "exampleapp"
        host = "example.com"
        autoVerified = false

        [[placeholders]]
        key = "locale"
        exampleValues = [ "en", "de" ]

        [[placeholders]]
        key = "user_id"
        exampleValues = [ "113748745" ]

    """.trimIndent()

    private val simpleDeepLink = """
        [deepLinks.home]
        patterns = [ "home" ]
    """.trimIndent()

    @Test
    fun `simple deep link`() = test(tomlHeader + simpleDeepLink) {
        assertThat(runTest()).isEqualTo(
            """
                pre content
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/home"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/home"
                        />
                </intent-filter>
                post content
            """.trimIndent(),
        )
    }

    private val deepLinkWithPlaceholder = """
        [deepLinks.plans]
        patterns = [ "{locale}/plans" ]
    """.trimIndent()

    @Test
    fun `deep link with placeholder`() = test(tomlHeader + deepLinkWithPlaceholder) {
        assertThat(runTest()).isEqualTo(
            """
                pre content
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/.*/plans"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/.*/plans"
                        />
                </intent-filter>
                post content
            """.trimIndent(),
        )
    }

    private val deepLinkWithMultiplePlaceholders = """
        [deepLinks.user_profile]
        patterns = [ "{locale}/users/{user_id}" ]

    """.trimIndent()

    @Test
    fun `deep link with multiple placeholders`() = test(tomlHeader + deepLinkWithMultiplePlaceholders) {
        assertThat(runTest()).isEqualTo(
            """
                pre content
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/.*/users/.*"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/.*/users/.*"
                        />
                </intent-filter>
                post content
            """.trimIndent(),
        )
    }

    private val deepLinkWithMultiplePatterns = """
        [deepLinks.user_profile_2]
        patterns = [ "users/{user_id}", "profiles/{user_id}" ]

    """.trimIndent()

    @Test
    fun `deep link with multiple patterns`() = test(tomlHeader + deepLinkWithMultiplePatterns) {
        assertThat(runTest()).isEqualTo(
            """
                pre content
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/profiles/.*"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/profiles/.*"
                        />
                </intent-filter>
                post content
            """.trimIndent(),
        )
    }

    private val deepLinkWithCustomPlaceholder = """
        [deepLinks.plan_by_slug]
        patterns = [ "{locale}/plans/{slug}" ]
        placeholders = [
          { key = "slug", exampleValues = [ "plan-foo", "plan-bar" ] },
        ]
    """.trimIndent()

    @Test
    fun `deep link with custom placeholder`() = test(tomlHeader + deepLinkWithCustomPlaceholder) {
        assertThat(runTest()).isEqualTo(
            """
                pre content
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/.*/plans/.*"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/.*/plans/.*"
                        />
                </intent-filter>
                post content
            """.trimIndent(),
        )
    }

    private val deepLinkWithCustomPrefixes = """
        [deepLinks.user_profile_short]
        patterns = [ "users/{user_id}", "profiles/{user_id}" ]
        prefixes = [
          { scheme = "https", host = "xmpl.com", autoVerified = true },
          { scheme = "exampleapp", host = "xmpl.com", autoVerified = false },
        ]
    """.trimIndent()

    @Test
    fun `deep link with custom prefixes`() = test(tomlHeader + deepLinkWithCustomPrefixes) {
        assertThat(runTest()).isEqualTo(
            """
                pre content
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="xmpl.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="https"
                        android:host="xmpl.com"
                        android:pathPattern="/profiles/.*"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="xmpl.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="xmpl.com"
                        android:pathPattern="/profiles/.*"
                        />
                </intent-filter>
                post content
            """.trimIndent(),
        )
    }

    private val multipleDeepLinks = listOf(
        simpleDeepLink,
        deepLinkWithPlaceholder,
        deepLinkWithMultiplePlaceholders,
        deepLinkWithMultiplePatterns,
        deepLinkWithCustomPlaceholder,
        deepLinkWithCustomPrefixes,
    ).joinToString(separator = "\n")

    @Test
    fun `multiple deep links`() = test(tomlHeader + multipleDeepLinks) {
        assertThat(runTest()).isEqualTo(
            """
                pre content
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/home"
                        />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/.*/plans"
                        />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/.*/users/.*"
                        />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/profiles/.*"
                        />
                    <data
                        android:scheme="https"
                        android:host="www.example.com"
                        android:pathPattern="/.*/plans/.*"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/home"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/.*/plans"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/.*/users/.*"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/profiles/.*"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="example.com"
                        android:pathPattern="/.*/plans/.*"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="true">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="https"
                        android:host="xmpl.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="https"
                        android:host="xmpl.com"
                        android:pathPattern="/profiles/.*"
                        />
                </intent-filter>
                <intent-filter android:autoVerify="false">
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:scheme="exampleapp"
                        android:host="xmpl.com"
                        android:pathPattern="/users/.*"
                        />
                    <data
                        android:scheme="exampleapp"
                        android:host="xmpl.com"
                        android:pathPattern="/profiles/.*"
                        />
                </intent-filter>
                post content
            """.trimIndent(),
        )
    }

    @Test
    fun `manifest without placeholder fails`() = test(inputManifestContent = "a\nb\nc") {
        val exception = assertThrows(IllegalStateException::class.java) {
            runTest()
        }

        assertThat(exception).hasMessageThat()
            .startsWith("Did not find <!-- DEEPLINK INTENT FILTERS --> in ")
    }

    @Test
    fun `no prefixes defined fails`() = test(simpleDeepLink) {
        val exception = assertThrows(IllegalStateException::class.java) {
            runTest()
        }

        assertThat(exception).hasMessageThat()
            .startsWith("Configuration contains deep links without a prefix but has no global prefixes")
    }

    @Test
    fun `no prefixes defined works if all deeplinks have their own`() = test(deepLinkWithCustomPrefixes) {
        runTest()
    }

    private fun test(
        configurationContent: String = SAMPLE_CONFIG,
        inputManifestContent: String = INPUT_MANIFEST_CONTENT.trimIndent(),
        testBlock: TestScope.() -> Unit,
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

    private val configFile: File
    private val inputManifestFile: File
    private val outputManifestFile: File

    init {
        configFile = temporaryFolder.newFile("config")
        configFile.writeText(configurationContent)

        inputManifestFile = temporaryFolder.newFile("inputManifest")
        inputManifestFile.writeText(inputManifestContent)

        outputManifestFile = temporaryFolder.newFile("outputManifest")
    }

    fun runTest(): String {
        configure(
            configFile,
            inputManifestFile,
            outputManifestFile,
        )
        return outputManifestFile.readText()
    }
}
