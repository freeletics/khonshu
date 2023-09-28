plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationApi")
}

dependencies {
    api(projects.navigation)
    api(libs.androidx.navigation.common)
    api(libs.androidx.navigation.runtime)
    api(libs.androidx.viewmodel.savedstate)

    implementation(libs.androidx.viewmodel)
    implementation(libs.uri)
}
