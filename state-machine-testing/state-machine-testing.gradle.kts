plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
}

dependencies {
    api(projects.stateMachine)
    api(libs.coroutines.core)
    api(libs.turbine)
}
