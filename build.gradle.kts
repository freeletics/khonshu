plugins {
    alias(libs.plugins.binarycompatibility)
    alias(libs.plugins.dependency.analysis)

    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.kapt).apply(false)
    alias(libs.plugins.kotlin.parcelize).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.publish).apply(false)
    alias(libs.plugins.gr8) apply(false)
    alias(libs.plugins.bestpractices).apply(false)
}

dependencyAnalysis {
    issues {
        all {
            ignoreKtx(true)

            onAny {
                severity("fail")
            }

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
                    ":navigator:navigator-runtime-compose",
                    ":navigator:navigator-runtime-fragment",
                    ":whetstone:navigation",
                    ":whetstone:navigation-compose",
                    ":whetstone:navigation-fragment",
                    ":whetstone:runtime",
                    ":whetstone:runtime-compose",
                    ":whetstone:runtime-fragment",
                )
            }
        }
    }

    abi {
        exclusions {
            excludeClasses(".*\\.navigator\\.internal\\..*")
        }
    }

    dependencies {
        bundle("androidx-compose-ui") {
            primary("androidx.compose.ui:ui")
            includeGroup("androidx.compose.ui")
        }
        bundle("androidx-compose-foundation") {
            primary("androidx.compose.foundation:foundation")
            includeGroup("androidx.compose.animation")
            includeGroup("androidx.compose.foundation")
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
