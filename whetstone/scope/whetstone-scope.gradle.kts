plugins {
    alias(libs.plugins.fgp.jvm)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
}

dependencies {
    api(libs.inject)
}
