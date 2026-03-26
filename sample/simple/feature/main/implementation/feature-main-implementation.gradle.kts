plugins {
    id("com.freeletics.gradle.feature")
}

freeletics {
    useKhonshu()
    useCompose()
}

dependencies {
    "androidMainApi"(libs.androidx.activity)
    commonMainApi(libs.compose.runtime)
    commonMainApi(libs.androidx.lifecycle.viewmodel.compose)
    commonMainApi(libs.androidx.lifecycle.viewmodel.savedstate)
    commonMainApi(libs.coroutines)
    commonMainApi(libs.khonshu.navigation)
    commonMainApi(libs.khonshu.statemachine)

    "androidMainApi"(libs.androidx.activity.compose)
    commonMainImplementation(libs.compose.ui)
    commonMainImplementation(libs.compose.foundation)
    commonMainImplementation(libs.androidx.lifecycle.common)
    commonMainImplementation(libs.androidx.lifecycle.viewmodel)
    commonMainImplementation(libs.khonshu.navigation)
    commonMainImplementation(projects.feature.root.nav)

    "kspAndroid"(libs.khonshu.codegen.compiler)
    "kspJvm"(libs.khonshu.codegen.compiler)
}
