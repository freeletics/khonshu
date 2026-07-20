buildscript {
    dependencies {
        // Remove when this is resolved: https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1661
        classpath("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.10")
    }
}

plugins {
    alias(libs.plugins.fgp.multiplatform).apply(false)
    alias(libs.plugins.fgp.gradle).apply(false)
    alias(libs.plugins.kotlin).apply(false)
    alias(libs.plugins.kotlin.parcelize).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.poko).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.publish).apply(false)
    alias(libs.plugins.dependency.analysis).apply(false)

    alias(libs.plugins.fgp.root)
}

dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                // auto-added by the Kotlin JS toolchain via the multiplatform plugin
                exclude("org.jetbrains.kotlin:kotlin-dom-api-compat")
            }
        }

        project(":codegen-compiler-test") {
            onUnusedDependencies {
                // needed for compile testing
                exclude(
                    "androidx.compose.runtime:runtime",
                    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
                    "org.jetbrains.kotlin:kotlin-compiler-embeddable",
                    "org.jetbrains.compose.ui:ui-desktop",
                    ":codegen",
                    ":codegen-compiler",
                    ":navigation-compose",
                    ":navigation-experimental",
                )
            }
        }
    }

    structure {
        bundle("compose-ui") {
            primary("org.jetbrains.compose.ui:ui")
            includeGroup("androidx.compose.ui")
            includeGroup("org.jetbrains.compose.ui")
        }
        bundle("compose-foundation") {
            primary("org.jetbrains.compose.foundation:foundation")
            includeGroup("androidx.compose.animation")
            includeGroup("androidx.compose.foundation")
            includeGroup("org.jetbrains.compose.animation")
            includeGroup("org.jetbrains.compose.foundation")
        }
        bundle("navigation-event") {
            primary("org.jetbrains.androidx.navigationevent:navigationevent-compose")
            includeGroup("androidx.navigationevent")
            includeGroup("org.jetbrains.androidx.navigationevent")
        }
        bundle("lifecycle-common") {
            primary("org.jetbrains.androidx.lifecycle:lifecycle-common")
            includeDependency("androidx.lifecycle:lifecycle-common")
            includeDependency("androidx.lifecycle:lifecycle-common-android")
            includeDependency("androidx.lifecycle:lifecycle-common-desktop")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-common")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-common-android")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-common-desktop")
        }
        bundle("lifecycle-runtime") {
            primary("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose")
            includeDependency("androidx.lifecycle:lifecycle-runtime")
            includeDependency("androidx.lifecycle:lifecycle-runtime-android")
            includeDependency("androidx.lifecycle:lifecycle-runtime-desktop")
            includeDependency("androidx.lifecycle:lifecycle-runtime-compose")
            includeDependency("androidx.lifecycle:lifecycle-runtime-compose-android")
            includeDependency("androidx.lifecycle:lifecycle-runtime-compose-desktop")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-runtime")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-runtime-android")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-runtime-desktop")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose-android")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose-desktop")
        }
        bundle("lifecycle-viewmodel") {
            primary("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel-android")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel-desktop")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel-savedstate")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel-savedstate-android")
            includeDependency("androidx.lifecycle:lifecycle-viewmodel-savedstate-desktop")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-android")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-desktop")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate-android")
            includeDependency("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate-desktop")
        }
        bundle("savedstate") {
            primary("androidx.savedstate:savedstate")
            includeGroup("androidx.savedstate")
        }
        bundle("uri-kmp") {
            primary("com.eygraber:uri-kmp")
            includeDependency("com.eygraber:uri-kmp")
            includeDependency("com.eygraber:uri-kmp-android")
            includeDependency("com.eygraber:uri-kmp-android-debug")
            includeDependency("com.eygraber:uri-kmp-jvm")
        }
    }
}
