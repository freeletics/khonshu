plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationApi")

    useCompose()
}

dependencies {
    api(projects.navigation)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.navigation.common)
    api(libs.androidx.navigation.runtime)

    implementation(projects.navigationAndroidx)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.compose)
}
