plugins {
    id("com.freeletics.gradle.multiplatform")
}

freeletics {
    enableOssPublishing()

    multiplatform {
        addAndroidTarget()
    }
}

dependencies {
    commonMainApi(projects.stateMachine)
    commonMainApi(libs.coroutines.core)
    commonMainApi(libs.turbine)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.coroutines.test)
}
