plugins {
    alias(libs.plugins.fgp.feature)
}

freeletics {
    enableCompose()
    useDaggerWithWhetstone()
}

dependencies {
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.activity.compose)
    implementation(libs.fl.navigator.runtime.compose)
    implementation(libs.fl.whetstone.runtime.compose)
    implementation(libs.fl.whetstone.scope)
    implementation(projects.feature.a.nav)
}
