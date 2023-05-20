plugins {
    alias(libs.plugins.fgp.app)
}

freeletics {
    applicationId("com.freeletics.mad.sample.simple")
    useDaggerWithComponent()
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.mad.navigator)
    implementation(libs.mad.navigator.compose)
    implementation(projects.feature.bottomSheet.implementation)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.main)
    implementation(projects.feature.screen.implementation)
    implementation(projects.feature.screen.nav)
    implementation(projects.feature.root.implementation)
    implementation(projects.feature.root.nav)
}
