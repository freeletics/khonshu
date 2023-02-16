plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.poko)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
    enableCompose()
    enableParcelize()
}

dependencies {
    api(projects.navigator.runtime)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)

    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.viewmodel.savedstate)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.parcelize)
}
