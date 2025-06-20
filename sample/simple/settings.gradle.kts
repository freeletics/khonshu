pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.27.0-alpha01")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
}
