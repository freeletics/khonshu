plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useKhonshu()
    useCompose()
}

dependencies {
    commonMainApi(libs.compose.runtime)
    commonMainApi(libs.androidx.lifecycle.viewmodel.compose)
    commonMainApi(libs.androidx.lifecycle.viewmodel.savedstate)
    commonMainApi(libs.coroutines)
    commonMainApi(libs.khonshu.navigation)
    commonMainApi(libs.khonshu.statemachine)
    commonMainApi(projects.feature.root.nav)

    commonMainImplementation(libs.compose.ui)
    commonMainImplementation(libs.compose.foundation)
    commonMainImplementation(projects.feature.bottomSheet.nav)
    commonMainImplementation(projects.feature.dialog.nav)
    commonMainImplementation(projects.feature.screen.nav)
    commonMainImplementation(projects.feature.newRoot.nav)
}
