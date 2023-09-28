plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationApi",
        "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
    )

    multiplatform {
        addJvmTarget()
        addAndroidTarget(publish = true)
    }

    useCompose()
}

dependencies {
    "commonMainApi"(projects.stateMachine)
    "commonMainApi"(projects.navigation)
    "commonMainApi"(libs.jetbrains.compose.runtime)
    "commonMainApi"(libs.inject)

    "androidMainApi"(libs.androidx.viewmodel)
    "androidMainApi"(libs.androidx.viewmodel.savedstate)
    "androidMainApi"(projects.stateMachine)

    "androidMainImplementation"(libs.coroutines.core)

    "androidMainCompileOnly"(libs.androidx.fragment)
    "androidMainCompileOnly"(libs.renderer)
}
