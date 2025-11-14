plugins {
    id("com.freeletics.gradle.multiplatform")
}

freeletics {
    useCompose()
    usePoko()
    useSerialization()
    enableOssPublishing()

    multiplatform {
        addAndroidTarget {
            enableParcelize()
        }
    }
}

dependencies {
    commonMainApi(libs.androidx.compose.runtime)

    commonMainImplementation(libs.androidx.compose.ui)
    "androidMainImplementation"(libs.kotlin.parcelize)

    "androidMainCompileOnly"(libs.androidx.annotations)

    "androidHostTestImplementation"(libs.kotlin.test)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.kotlinx.serialization.json)
}
