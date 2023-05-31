plugins {
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.kapt).apply(false)
    alias(libs.plugins.kotlin.parcelize).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.publish).apply(false)
    alias(libs.plugins.gr8) apply (false)
    alias(libs.plugins.dependency.analysis).apply(false)
    alias(libs.plugins.bestpractices).apply(false)

    alias(libs.plugins.fgp.root)
    alias(libs.plugins.binarycompatibility)
}

dependencyAnalysis {
    issues {
        all {
            onCompileOnly {
                exclude("dev.drewhamilton.poko:poko-annotations")
            }
        }

        project(":whetstone:runtime") {
            onUnusedDependencies {
                exclude(":whetstone:scope")
            }
            onIncorrectConfiguration {
                exclude(":whetstone:scope")
            }
        }

        project(":whetstone:runtime-compose") {
            onUnusedDependencies {
                exclude(":whetstone:runtime")
            }
            onIncorrectConfiguration {
                exclude(":whetstone:runtime")
            }
        }

        project(":whetstone:runtime-fragment") {
            onUnusedDependencies {
                exclude(":whetstone:runtime")
            }
            onIncorrectConfiguration {
                exclude(":whetstone:runtime")
            }
        }

        project(":whetstone:navigation") {
            onUnusedDependencies {
                exclude(":whetstone:runtime")
            }
            onIncorrectConfiguration {
                exclude(":whetstone:runtime")
            }
        }

        project(":whetstone:navigation-compose") {
            onUnusedDependencies {
                exclude(":whetstone:navigation", ":whetstone:runtime-compose")
            }
            onIncorrectConfiguration {
                exclude(":whetstone:navigation", ":whetstone:runtime-compose")
            }
        }

        project(":whetstone:navigation-fragment") {
            onUnusedDependencies {
                exclude(":whetstone:navigation", ":whetstone:runtime-fragment")
            }
            onIncorrectConfiguration {
                exclude(":whetstone:navigation", ":whetstone:runtime-fragment")
            }
        }

        project(":whetstone:compiler-test") {
            onUnusedDependencies {
                // needed for compile testing
                exclude(
                    "androidx.compose.runtime:runtime",
                    "com.gabrielittner.renderer:renderer",
                    "com.gabrielittner.renderer:connect",
                    "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
                    "org.jetbrains.kotlin:kotlin-compiler-embeddable",
                    ":navigator:runtime-compose",
                    ":navigator:runtime-fragment",
                    ":whetstone:navigation",
                    ":whetstone:navigation-compose",
                    ":whetstone:navigation-fragment",
                    ":whetstone:runtime",
                    ":whetstone:runtime-compose",
                    ":whetstone:runtime-fragment",
                )
            }
        }

        project(":navigator:runtime") {
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

    abi {
        exclusions {
            excludeClasses(".*\\.navigator\\.internal\\..*")
        }
    }
}

apiValidation {
    ignoredProjects += arrayOf("compiler")

    nonPublicMarkers += arrayOf(
        "com.freeletics.mad.navigator.internal.InternalNavigatorApi",
        "com.freeletics.mad.whetstone.internal.InternalWhetstoneApi",
    )
}
