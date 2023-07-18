plugins {
    alias(libs.plugins.fgp.feature)
}

freeletics {
    enableCompose()
    useDaggerWithKhonshu()
}

dependencies {
    api(libs.androidx.activity)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.khonshu.navigator.compose)
    api(libs.khonshu.statemachine)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.khonshu.navigator)
    implementation(libs.khonshu.codegen)
    implementation(libs.khonshu.codegen.compose)
    implementation(libs.khonshu.codegen.scope)
    implementation(projects.feature.root.nav)
}
