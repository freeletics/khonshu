plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigation.internal.InternalNavigationApi")
    enableCompose()
}

dependencies {
    api(projects.navigation)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.navigation.common)

    implementation(projects.navigationAndroidx)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.navigation.compose)
}
