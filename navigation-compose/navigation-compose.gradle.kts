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
    api(libs.androidx.viewmodel.savedstate)
    api(libs.collections.immutable)

    implementation(libs.coroutines.core)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.navigation.compose)
}
