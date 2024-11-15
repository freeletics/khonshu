plugins {
    id("com.freeletics.gradle.android")
    id("com.freeletics.gradle.publish.oss")
}

freeletics {
    useCompose()
    usePoko()

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
