plugins {
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.parcelize).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.publish).apply(false)
    alias(libs.plugins.gr8).apply(false)
    alias(libs.plugins.dependency.analysis).apply(false)
    alias(libs.plugins.bestpractices).apply(false)

    alias(libs.plugins.fgp.root)
    alias(libs.plugins.binarycompatibility)
}

dependencyAnalysis {
    issues {
        all {
            onUsedTransitiveDependencies {
                exclude("dev.drewhamilton.poko:poko-annotations")
            }
        }

        project(":codegen-scope") {
            onUnusedDependencies {
                exclude(":codegen")
            }
        }

        project(":codegen-compose") {
            onUnusedDependencies {
                exclude(":codegen")
            }
        }

        project(":codegen-fragment") {
            onUnusedDependencies {
                exclude(":codegen")
            }
        }

        project(":codegen-compiler") {
            onUnusedDependencies {
                exclude("dev.zacsweers.kctfork:core", "com.squareup.anvil:compiler-utils")
            }
        }

        project(":codegen-compiler-test") {
            onUnusedDependencies {
                // needed for compile testing
                exclude(
                    "androidx.compose.runtime:runtime",
                    "com.gabrielittner.renderer:renderer",
                    "com.gabrielittner.renderer:connect",
                    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
                    "org.jetbrains.kotlin:kotlin-compiler-embeddable",
                    "com.squareup.anvil:annotations-optional",
                    "com.squareup.anvil:compiler-utils",
                    ":codegen",
                    ":codegen-compiler",
                    ":codegen-compose",
                    ":codegen-fragment",
                    ":navigation-compose",
                    ":navigation-fragment",
                )
            }
        }

        project(":navigation") {
            onUsedTransitiveDependencies {
                exclude(
                    // false positive
                    "org.checkerframework:checker-qual",
                )
            }
        }

        project(":deeplinks-plugin") {
            onIncorrectConfiguration {
                // suggests junit to be api instead of testImplementation
                exclude("junit:junit")
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
