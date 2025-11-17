plugins {
    id("com.freeletics.gradle.multiplatform")
}

freeletics {
    // enableOssPublishing()

    multiplatform {
        addCommonTargets()
    }
}

dependencies {
    "commonMainApi"(libs.coroutines.core)
    "commonMainApi"(projects.stateMachine)
}
