plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
}

dependencies {
    api(projects.stateMachine.runtime)
    api(libs.coroutines.core)
    api(libs.turbine)
}
