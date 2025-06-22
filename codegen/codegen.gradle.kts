plugins {
    id("com.freeletics.gradle.multiplatform")
    id("com.freeletics.gradle.publish.oss")
}

freeletics {
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi",
        "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
    )

    multiplatform {
        addJvmTarget()
        addAndroidTarget()
    }

    useCompose()
}

dependencies {
    "commonMainApi"(projects.stateMachine)
    "commonMainApi"(projects.navigation)
    "commonMainApi"(libs.androidx.compose.runtime)
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
