enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.2.2")
}

rootProject.name = "mad"

// TODO find a better workaround
project(":navigator:runtime").name = "navigator-runtime"
project(":navigator:runtime-compose").name = "navigator-runtime-compose"
project(":navigator:runtime-fragment").name = "navigator-runtime-fragment"
