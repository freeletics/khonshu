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
    androidMainApi(projects.feature.screenWithResult.nav)

    androidMainImplementation(libs.androidx.compose.ui)
    androidMainImplementation(libs.androidx.compose.foundation)
    androidMainImplementation(libs.androidx.compose.material)
}
