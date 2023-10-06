plugins {
    alias(libs.plugins.fgp.gradle)
    alias(libs.plugins.fgp.publish)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.toml)
    implementation(libs.serialization)
    implementation(projects.navigation)

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
