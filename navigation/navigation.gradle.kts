plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.poko)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationApi")
    enableParcelize()
}

android {
    defaultConfig {
        consumerProguardFile(project.file("navigation.pro"))
    }
}

dependencies {
    api(libs.androidx.activity)
    api(libs.androidx.core)
    api(libs.androidx.lifecycle.common)
    api(libs.androidx.viewmodel.savedstate)
    api(libs.coroutines.core)
    api(libs.uri)

    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.kotlin.parcelize)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.lifecycle.testing)
    testImplementation(libs.coroutines.test)
}
