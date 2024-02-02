plugins {
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.parcelize).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.publish).apply(false)
    alias(libs.plugins.dependency.analysis).apply(false)

    alias(libs.plugins.fgp.root)
    alias(libs.plugins.binarycompatibility)
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
                    "com.squareup.anvil:annotations-optional",
                    "com.squareup.anvil:compiler-utils",
                    ":codegen",
                    ":codegen-compiler",
                    ":navigation-compose",
                    ":navigation-experimental",
                )
            }
        }
    }
}

apiValidation {
    ignoredProjects += arrayOf("codegen-compiler", "codegen-compiler-test")

    nonPublicMarkers += arrayOf(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationApi",
        "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
    )
}
