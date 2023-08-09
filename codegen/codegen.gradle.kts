plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationApi",
        "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
    )

    android {
        enableCompose()
    }
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.viewmodel)
    api(libs.androidx.viewmodel.savedstate)
    api(projects.stateMachine)
    api(projects.codegenScope)
    api(projects.navigation)

    implementation(libs.coroutines.core)
}
