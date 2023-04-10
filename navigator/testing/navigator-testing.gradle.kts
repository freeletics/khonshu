plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
}

dependencies {
    api(projects.navigator.runtime)
    api(libs.coroutines.core)
    api(libs.turbine)
    implementation(libs.androidx.activity)
    implementation(libs.truth)
}
