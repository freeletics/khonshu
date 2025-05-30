pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("kotlin-2.2.0-SNAPSHOT")
}

rootProject.name = "simple-sample"

freeletics {
    snapshots()
    includeKhonshu("../..")
}
