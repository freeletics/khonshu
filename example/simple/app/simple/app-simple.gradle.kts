plugins {
    alias(libs.plugins.fl.app)
}

freeletics {
    applicationId("com.freeletics.mad.example.simple")
    useDaggerWithComponent()
}

dependencies {
    implementation(libs.fl.navigator.runtime.compose)
    implementation(projects.feature.a.implementation)
    implementation(projects.feature.a.nav)
    implementation(projects.feature.main)
}
