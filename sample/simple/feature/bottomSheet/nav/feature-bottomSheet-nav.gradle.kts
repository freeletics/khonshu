plugins {
    id("com.freeletics.gradle.nav")
}

freeletics {
}

dependencies {
    commonMainApi(libs.khonshu.codegen.runtime)
    commonMainApi(libs.khonshu.navigation)
}
