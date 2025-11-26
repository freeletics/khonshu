plugins {
    id("com.freeletics.gradle.multiplatform")
}

freeletics {
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi",
        "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
    )

    useCompose()
    enableOssPublishing()

    multiplatform {
        addJvmTarget()
        addAndroidTarget()
    }
}

@OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
kotlin {
    abiValidation {
        filters {
            excluded {
                annotatedWith.addAll(
                    "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
                )
            }
        }
    }
}

dependencies {
    "commonMainApi"(projects.stateMachine)
    "commonMainApi"(projects.navigation)
    "commonMainApi"(libs.androidx.compose.runtime)
    "commonMainApi"(libs.jetbrains.compose.ui)
    "commonMainApi"(libs.metro)
    "commonMainApi"(libs.androidx.lifecycle.runtime)
    "commonMainApi"(libs.androidx.lifecycle.compose)
    "commonMainApi"(libs.androidx.viewmodel)
    "commonMainApi"(libs.androidx.viewmodel.savedstate)
    "commonMainApi"(libs.coroutines.core)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.coroutines.test)
    "androidHostTestImplementation"(libs.turbine)
    "androidHostTestImplementation"(libs.androidx.lifecycle.testing)
}
