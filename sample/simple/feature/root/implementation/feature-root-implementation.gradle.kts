plugins {
    id("com.freeletics.gradle.feature.android")
}

freeletics {
    useKhonshu()
    useCompose()
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.khonshu.navigation)
    api(libs.khonshu.statemachine)
    api(projects.feature.root.nav)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.dialog.nav)
    implementation(projects.feature.screen.nav)
    implementation(projects.feature.newRoot.nav)
}
