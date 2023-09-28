plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationApi")
}

dependencies {
    api(projects.navigation)
    api(libs.androidx.fragment)
    api(libs.androidx.navigation.fragment)

    implementation(projects.navigationAndroidx)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.runtime)
}
