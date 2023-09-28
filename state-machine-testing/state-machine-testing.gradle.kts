plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
}

dependencies {
    api(projects.stateMachine)
    api(libs.coroutines.core)
    api(libs.turbine)
}
