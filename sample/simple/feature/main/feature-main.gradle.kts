plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useDaggerWithKhonshu()
    useCompose()
}

dependencies {
    api(libs.androidx.activity)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.khonshu.navigation)
    api(libs.khonshu.statemachine)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.khonshu.navigation)
    implementation(projects.feature.root.nav)
}
