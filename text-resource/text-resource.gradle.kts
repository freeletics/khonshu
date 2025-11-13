plugins {
    id("com.freeletics.gradle.android")
    id("com.freeletics.gradle.publish.oss")
}

freeletics {
    useCompose()
    usePoko()
    useSerialization()

    android {
        enableParcelize()
    }
}

dependencies {
    api(libs.androidx.compose.runtime)

    implementation(libs.androidx.compose.ui)
    implementation(libs.kotlin.parcelize)

    compileOnly(libs.androidx.annotations)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.serialization.json)
}
