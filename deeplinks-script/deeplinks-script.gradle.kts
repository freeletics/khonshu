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
    "jvmMainApi"("com.github.ajalt.clikt:clikt:5.1.0")
    "jvmMainImplementation"(projects.navigationTesting)
    "jvmMainImplementation"("dev.mobile:dadb:1.2.10")
}
