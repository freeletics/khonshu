pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
        mavenLocal()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("metro-SNAPSHOT")
}

rootProject.name = "simple-sample"

freeletics {
    snapshots()
    includeKhonshu("../..")
}
