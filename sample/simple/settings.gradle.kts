pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.8.1")
}

rootProject.name = "simple-sample"

configure<com.freeletics.gradle.plugin.SettingsExtension> {
    includeKhonshu("../..")
}
