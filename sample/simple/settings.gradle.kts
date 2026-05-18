pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.37.0")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
}
