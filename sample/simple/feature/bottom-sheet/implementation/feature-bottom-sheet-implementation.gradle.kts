plugins {
    alias(libs.plugins.fgp.feature)
}

freeletics {
    useDaggerWithKhonshu()

    android {
        enableCompose()
    }
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.khonshu.navigator)
    api(libs.khonshu.navigator.compose)
    api(libs.khonshu.statemachine)
    api(libs.khonshu.codegen.compose)
    api(projects.feature.bottomSheet.nav)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.khonshu.codegen)
    implementation(libs.khonshu.codegen.scope)
    implementation(projects.feature.dialog.nav)
    implementation(projects.feature.screen.nav)
}
