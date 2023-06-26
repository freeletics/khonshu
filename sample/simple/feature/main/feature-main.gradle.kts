plugins {
    alias(libs.plugins.fgp.feature)
}

freeletics {
    enableCompose()
    useDaggerWithWhetstone()
}

dependencies {
    api(libs.androidx.activity)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.coroutines)
    api(libs.mad.navigator.compose)
    api(libs.mad.statemachine)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.mad.navigator)
    implementation(libs.mad.whetstone.runtime)
    implementation(libs.mad.whetstone.runtime.compose)
    implementation(libs.mad.whetstone.scope)
    implementation(projects.feature.a.nav)
}
