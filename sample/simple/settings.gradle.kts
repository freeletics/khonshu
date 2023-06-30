pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.3.8")
}

rootProject.name = "simple-sample"

configure<com.freeletics.gradle.plugin.SettingsExtension> {
    includeKhonshu("../..")
}
