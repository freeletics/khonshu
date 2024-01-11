plugins {
    alias(libs.plugins.fgp.feature)
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
    api(libs.khonshu.navigator)
    api(libs.khonshu.navigator.compose)
    api(libs.khonshu.statemachine)
    api(libs.khonshu.codegen)
    api(projects.feature.newRoot.nav)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.dialog.nav)
    implementation(projects.feature.root.nav)
    implementation(projects.feature.screen.nav)
}
