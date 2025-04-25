import com.android.build.api.dsl.CommonExtension

plugins {
    id("com.freeletics.gradle.multiplatform")
    id("com.freeletics.gradle.publish.oss")
}

freeletics {
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi",
        "com.freeletics.khonshu.codegen.internal.InternalCodegenApi",
    )

    multiplatform {
        addJvmTarget()
        addAndroidTarget()
    }

    useCompose()
}

extensions.configure(CommonExtension::class.java) {
    lint {
        disable.add("UnsafeOptInUsageError")
    }
}

dependencies {
    "commonMainApi"(projects.stateMachine)
    "commonMainApi"(projects.navigation)
    "commonMainApi"(libs.jetbrains.compose.runtime)
    "commonMainApi"(libs.jetbrains.compose.ui)
    "commonMainApi"(libs.metro)

    "androidMainApi"(libs.androidx.viewmodel)
    "androidMainApi"(libs.androidx.viewmodel.savedstate)
    "androidMainApi"(projects.stateMachine)

    "androidMainImplementation"(libs.coroutines.core)

    "androidUnitTestImplementation"(libs.junit)
    "androidUnitTestImplementation"(libs.truth)
    "androidUnitTestImplementation"(libs.coroutines.test)
    "androidUnitTestImplementation"(libs.turbine)
    "androidUnitTestImplementation"(libs.androidx.lifecycle.testing)
}
