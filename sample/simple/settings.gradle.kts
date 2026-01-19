pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.36.1")
}

rootProject.name = "simple-sample"

freeletics {
//    includeKhonshu("../..")
}
