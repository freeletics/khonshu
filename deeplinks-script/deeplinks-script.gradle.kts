plugins {
    id("com.freeletics.gradle.multiplatform")
}

freeletics {
    multiplatform {
        addJvmTarget()
    }

    enableOssPublishing()
}

dependencies {
    "jvmMainApi"(libs.clikt)
    "jvmMainApi"(libs.clikt.core)
    "jvmMainImplementation"(libs.mordant.core)
    "jvmMainImplementation"(projects.navigation)
    "jvmMainImplementation"(projects.navigationTesting)
    "jvmMainImplementation"(libs.dadb)
    "jvmMainImplementation"(libs.uri)
}
