plugins {
    alias(libs.plugins.fl.gradle)
    alias(libs.plugins.publish)
}

dependencies {
    compileOnly(libs.android.gradle.api)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

gradlePlugin {
//    plugins {
//        plugin {
//            id = "com.freeletics.gradle.deeplinks"
//            implementationClass = "com.freeletics.gradle.plugin.DeeplinksPlugin"
//        }
//    }
}
