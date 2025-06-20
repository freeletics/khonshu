pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.27.0-alpha01")
}

rootProject.name = "simple-sample"

freeletics {
    snapshots()
    includeKhonshu("../..")
}
