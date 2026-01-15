pluginManagement {
    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    name = "Sonatype Snapshots"
                    url = uri("https://central.sonatype.com/repository/maven-snapshots/")
                }
            }

            filter {
                includeGroup("dev.zacsweers.metro")
            }
        }

        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.36.3")
}

rootProject.name = "simple-sample"

freeletics {
    includeKhonshu("../..")
    snapshots()
}
