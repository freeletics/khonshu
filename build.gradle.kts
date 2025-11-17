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
