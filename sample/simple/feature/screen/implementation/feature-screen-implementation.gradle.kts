plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useDaggerWithKhonshu()
    useCompose()
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.khonshu.navigation)
    api(libs.khonshu.statemachine)
    api(projects.feature.screen.nav)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.dialog.nav)
    implementation(projects.feature.newRoot.nav)
    implementation(projects.feature.screenWithResult.nav)
}
