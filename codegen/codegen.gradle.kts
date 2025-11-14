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
    "commonMainApi"(libs.jetbrains.compose.runtime)
    "commonMainApi"(libs.jetbrains.compose.ui)
    "commonMainApi"(libs.metro)

    "androidMainApi"(libs.androidx.viewmodel)
    "androidMainApi"(libs.androidx.viewmodel.savedstate)
    "androidMainApi"(projects.stateMachine)

    "androidMainImplementation"(libs.coroutines.core)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.coroutines.test)
    "androidHostTestImplementation"(libs.turbine)
    "androidHostTestImplementation"(libs.androidx.lifecycle.testing)
}
