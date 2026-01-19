plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useKhonshu()
    useCompose()
}

dependencies {
    androidMainApi(libs.androidx.activity)
    androidMainApi(libs.androidx.compose.runtime)
    androidMainApi(libs.androidx.lifecycle.viewmodel.compose)
    androidMainApi(libs.androidx.lifecycle.viewmodel.savedstate)
    androidMainApi(libs.coroutines)
    androidMainApi(libs.khonshu.navigation)
    androidMainApi(libs.khonshu.statemachine)

    androidMainImplementation(libs.androidx.activity.compose)
    androidMainImplementation(libs.androidx.compose.ui)
    androidMainImplementation(libs.androidx.compose.foundation)
    androidMainImplementation(libs.androidx.lifecycle.common)
    androidMainImplementation(libs.androidx.lifecycle.viewmodel)
    androidMainImplementation(libs.khonshu.navigation)
    androidMainImplementation(projects.feature.root.nav)
}
