plugins {
    alias(libs.plugins.fgp.app)
}

freeletics {
    applicationId("com.freeletics.mad.sample.simple")
    useDaggerWithComponent()
}

dependencies {
    implementation(libs.fl.navigator.runtime.compose)
    implementation(projects.feature.a.implementation)
    implementation(projects.feature.a.nav)
    implementation(projects.feature.bottomSheet.implementation)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.main)
}
