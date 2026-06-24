pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.38.2")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
}
