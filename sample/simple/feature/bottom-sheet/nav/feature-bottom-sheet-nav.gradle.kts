plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useSerialization()
}

dependencies {
    api(libs.khonshu.codegen.runtime)
    api(libs.khonshu.navigation)
}
