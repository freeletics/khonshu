plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn(
        "com.freeletics.mad.navigation.internal.InternalNavigationApi",
        "com.freeletics.mad.codegen.internal.InternalCodegenApi",
    )
    enableCompose()
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.viewmodel)
    api(libs.androidx.viewmodel.savedstate)
    api(projects.stateMachine)
    api(projects.codegenScope)
    api(projects.navigation)
    api(libs.dagger)

    implementation(libs.coroutines.core)
}
