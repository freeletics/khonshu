pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.35.0")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
}
