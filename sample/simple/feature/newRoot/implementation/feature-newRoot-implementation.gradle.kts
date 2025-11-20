plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useKhonshu()
    useCompose()
}

dependencies {
    androidMainApi(libs.androidx.compose.runtime)
    androidMainApi(libs.androidx.lifecycle.viewmodel.compose)
    androidMainApi(libs.androidx.lifecycle.viewmodel.savedstate)
    androidMainApi(libs.coroutines)
    androidMainApi(libs.khonshu.navigation)
    androidMainApi(libs.khonshu.statemachine)
    androidMainApi(projects.feature.newRoot.nav)

    androidMainImplementation(libs.androidx.compose.ui)
    androidMainImplementation(libs.androidx.compose.foundation)
    androidMainImplementation(projects.feature.bottomSheet.nav)
    androidMainImplementation(projects.feature.dialog.nav)
    androidMainImplementation(projects.feature.root.nav)
    androidMainImplementation(projects.feature.screen.nav)
}
