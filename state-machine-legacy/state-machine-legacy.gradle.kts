plugins {
    id("com.freeletics.gradle.multiplatform")
    id("com.freeletics.gradle.publish.oss")
}

freeletics {
    multiplatform {
        addCommonTargets()
    }
}

dependencies {
    "commonMainApi"(libs.coroutines.core)
    "commonMainApi"(projects.stateMachine)
}
