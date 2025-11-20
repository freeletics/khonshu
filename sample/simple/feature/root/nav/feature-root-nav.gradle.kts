plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useSerialization()
}

dependencies {
    androidMainApi(libs.khonshu.navigation)
}
