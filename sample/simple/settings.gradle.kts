pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.30.2")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
}
