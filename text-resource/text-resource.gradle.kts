plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.poko)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    useCompose()

    android {
        enableParcelize()
    }
}

dependencies {
    api(libs.androidx.compose.runtime)

    implementation(libs.androidx.compose.ui)
    implementation(libs.kotlin.parcelize)

    compileOnly(libs.androidx.annotations)
}
