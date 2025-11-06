pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.33.1")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
}
