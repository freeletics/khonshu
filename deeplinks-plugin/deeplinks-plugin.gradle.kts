plugins {
    alias(libs.plugins.fgp.gradle)
    alias(libs.plugins.fgp.publish)
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
