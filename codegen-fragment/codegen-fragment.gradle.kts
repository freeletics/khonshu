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
    api(projects.codegen)
    api(projects.navigation)
    api(libs.androidx.fragment)

    implementation(projects.codegenScope)
    implementation(projects.navigationFragment)
    implementation(projects.stateMachine)

    compileOnly(libs.renderer)
}
