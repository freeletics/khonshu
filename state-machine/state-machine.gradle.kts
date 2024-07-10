plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    multiplatform {
        addCommonTargets()
    }
}

dependencies {
    "commonMainApi"(libs.coroutines.core)
}
