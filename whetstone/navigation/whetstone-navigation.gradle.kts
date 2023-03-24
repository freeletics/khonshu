plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
    optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
}

dependencies {
    api(projects.whetstone.runtime)
    api(projects.navigator.navigatorRuntime)
    api(libs.inject)
    api(libs.dagger)

    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.savedstate)
}
