enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.3.7")
}

rootProject.name = "khonshu"
