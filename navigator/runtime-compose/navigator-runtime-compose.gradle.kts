plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
    enableCompose()
}

dependencies {
    api(projects.navigator.navigatorRuntime)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.navigation.runtime)

    implementation(projects.navigator.androidxNav)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.navigation)
}
