plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    addCommonTargets()
}

dependencies {
    "commonMainApi"(libs.coroutines.core)
}
