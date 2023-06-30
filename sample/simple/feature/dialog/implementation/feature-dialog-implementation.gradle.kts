plugins {
    alias(libs.plugins.fgp.feature)
}

freeletics {
    enableCompose()
    useDaggerWithKhonshu()
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.khonshu.navigator)
    api(libs.khonshu.navigator.compose)
    api(libs.khonshu.statemachine)
    api(libs.khonshu.codegen.compose)
    api(projects.feature.dialog.nav)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.khonshu.codegen)
    implementation(libs.khonshu.codegen.scope)
}
