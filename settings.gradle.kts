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
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.12.1")
}

rootProject.name = "khonshu"
