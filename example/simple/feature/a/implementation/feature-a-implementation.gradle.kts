plugins {
    alias(libs.plugins.fl.feature)
}

freeletics {
    enableCompose()
    useDaggerWithWhetstone()
}

dependencies {
    implementation(libs.androidx.annotations)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.fl.navigator.runtime.compose)
    implementation(libs.fl.whetstone.runtime.compose)
    implementation(libs.fl.whetstone.scope)
    implementation(projects.feature.a.nav)
}
