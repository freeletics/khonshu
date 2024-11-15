plugins {
    id("com.freeletics.gradle.android")
    id("com.freeletics.gradle.publish.oss")
}

freeletics {
}

dependencies {
    api(projects.stateMachine)
    api(libs.coroutines.core)
    api(libs.turbine)
}
