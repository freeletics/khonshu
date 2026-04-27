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
        addIosTargets()
    }
}

@OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
kotlin {
    abiValidation {
        filters {
            exclude {
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

    "androidMainImplementation"(libs.androidx.compose.ui)
    "androidMainImplementation"(libs.androidx.lifecycle.common.android)
    "androidMainImplementation"(libs.androidx.lifecycle.runtime.android)
    "androidMainImplementation"(libs.androidx.lifecycle.compose.android)

    "jvmMainApi"(projects.navigation)
    "jvmMainApi"(projects.stateMachine)
    "jvmMainImplementation"(libs.androidx.lifecycle.common.android)
    "jvmMainImplementation"(libs.androidx.lifecycle.runtime.desktop)
    "jvmMainImplementation"(libs.androidx.lifecycle.compose.desktop)
    "jvmMainImplementation"(libs.jetbrains.compose.ui.desktop)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.coroutines.test)
    "androidHostTestImplementation"(libs.turbine)
    "androidHostTestImplementation"(libs.androidx.lifecycle.testing)
    "androidHostTestImplementation"(libs.androidx.lifecycle.common.android)
    "androidHostTestImplementation"(libs.androidx.lifecycle.runtime.android)
}
