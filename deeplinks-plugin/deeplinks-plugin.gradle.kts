plugins {
    id("com.freeletics.gradle.gradle")
    id("com.freeletics.gradle.publish.oss")
}

dependencies {
    implementation(projects.navigationTesting)

    compileOnly(libs.android.gradle.api)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

gradlePlugin {
    plugins {
        create("plugin") {
            id = "com.freeletics.khonshu.deeplinks"
            implementationClass = "com.freeletics.khonshu.deeplinks.plugin.DeeplinksPlugin"
        }
    }
}
