plugins {
    alias(libs.plugins.fgp.feature)
}

freeletics {
    enableCompose()
    useDaggerWithWhetstone()
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.mad.navigator)
    api(libs.mad.navigator.compose)
    api(libs.mad.statemachine)
    api(libs.mad.codegen.compose)
    api(projects.feature.root.nav)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.mad.codegen)
    implementation(libs.mad.codegen.scope)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.dialog.nav)
    implementation(projects.feature.screen.nav)
}
