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
            onAny {
                // issue with Kotlin multiplatform artifacts
                exclude(
                    "com.eygraber:uri-kmp",
                    "com.eygraber:uri-kmp-android",
                    "com.eygraber:uri-kmp-android-debug",
                )
            }
            onUnusedDependencies {
                // auto-added by the Kotlin JS toolchain via the multiplatform plugin
                exclude("org.jetbrains.kotlin:kotlin-dom-api-compat")
            }
        }

        project(":navigation") {
            onUnusedDependencies {
                exclude(
                    "org.jetbrains.compose.ui:ui",
                    "org.jetbrains.compose.foundation:foundation",
                    "org.jetbrains.androidx.lifecycle:lifecycle-common",
                    "org.jetbrains.androidx.navigationevent:navigationevent-compose",
                )
            }
        }

        project(":codegen") {
            onUnusedDependencies {
                exclude(
                    "org.jetbrains.compose.ui:ui",
                    "org.jetbrains.androidx.lifecycle:lifecycle-runtime",
                    "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose",
                    "androidx.lifecycle:lifecycle-viewmodel",
                    "androidx.lifecycle:lifecycle-viewmodel-savedstate",
                )
            }
        }

        project(":codegen-compiler-test") {
            onUnusedDependencies {
                // needed for compile testing
                exclude(
                    "androidx.compose.runtime:runtime",
                    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
                    "org.jetbrains.kotlin:kotlin-compiler-embeddable",
                    ":codegen",
                    ":codegen-compiler",
                    ":navigation-compose",
                    ":navigation-experimental",
                )
            }
        }
    }
}
