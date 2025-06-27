pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.27.3")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
}
