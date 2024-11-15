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
    api(libs.khonshu.codegen)
    api(projects.feature.dialog.nav)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
}
