plugins {
    alias(libs.plugins.fgp.gradle)
//    alias(libs.plugins.publish)
}

dependencies {
    compileOnly(libs.android.gradle.api)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

gradlePlugin {
    plugins {
        create("plugin") {
            id = "com.freeletics.mad.deeplinks.plugin"
            implementationClass = "com.freeletics.mad.deeplinks.plugin.DeeplinksPlugin"
        }
    }
}
