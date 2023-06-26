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
    api(projects.codegen)
    api(projects.navigation)
    api(libs.androidx.fragment)

    implementation(projects.codegenScope)
    implementation(projects.navigationFragment)
    implementation(projects.stateMachine)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.savedstate)

    compileOnly(libs.renderer)
}
