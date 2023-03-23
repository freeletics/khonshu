plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
}

dependencies {
    api(projects.navigator.navigatorRuntime)
    api(libs.androidx.fragment)
    api(libs.androidx.navigation.fragment)

    implementation(projects.navigator.androidxNav)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.runtime)
}
