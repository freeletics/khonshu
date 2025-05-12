pluginManagement {
    repositories {
        exclusiveContent {
            forRepository { google() }

            filter {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com.google.firebase.*")

                includeGroup("com.google.testing.platform")
                includeGroup("com.google.android.apps.common.testing.accessibility.framework")
            }
        }

        exclusiveContent {
            forRepository { gradlePluginPortal() }

            filter {
                includeGroupByRegex("com.gradle.*")
                includeGroupByRegex("org.gradle.*")
            }
        }

        mavenCentral()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("kotlin-2.2.0-SNAPSHOT")
}

rootProject.name = "khonshu"

freeletics {
    snapshots()
}
