import com.freeletics.gradle.plugin.FreeleticsAndroidExtension

plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationApi",
        "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
    )

    multiplatform {
        addJvmTarget()
        addAndroidTarget(publish = true)
    }

    extensions.configure(FreeleticsAndroidExtension::class) {
        enableCompose()
    }
}

dependencies {
    "commonMainApi"(projects.stateMachine)
    "commonMainApi"(projects.navigation)
    "commonMainApi"(libs.inject)

    "androidMainApi"(libs.androidx.compose.runtime)
    "androidMainApi"(libs.androidx.viewmodel)
    "androidMainApi"(libs.androidx.viewmodel.savedstate)
    "androidMainApi"(projects.stateMachine)

    "androidMainImplementation"(libs.coroutines.core)

    "androidMainCompileOnly"(libs.androidx.fragment)
    "androidMainCompileOnly"(libs.renderer)
}
