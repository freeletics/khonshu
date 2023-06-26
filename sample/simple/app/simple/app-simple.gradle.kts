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
    implementation(projects.feature.a.implementation)
    implementation(projects.feature.a.nav)
    implementation(projects.feature.bottomSheet.implementation)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.main)
}
