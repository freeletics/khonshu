plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationApi")
}

dependencies {
    api(projects.navigation)
    api(libs.coroutines.core)
    api(libs.turbine)
    implementation(libs.androidx.activity)
    implementation(libs.truth)
}
