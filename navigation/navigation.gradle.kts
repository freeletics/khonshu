plugins {
    id("com.freeletics.gradle.multiplatform")
}

freeletics {
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationApi",
        "com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi",
        "com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi",
    )

    usePoko()
    useSerialization()
    useCompose()
    enableOssPublishing()

    multiplatform {
        addJvmTarget()
        addAndroidTarget {
            consumerProguardFiles("navigation.pro")
        }
        addIosTargets()
    }
}

@OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
kotlin {
    abiValidation {
        filters {
            exclude {
                annotatedWith.addAll(
                    "com.freeletics.khonshu.navigation.internal.InternalNavigationApi",
                    "com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi",
                    "com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi",
                )
            }
        }
    }
}

dependencies {
    "commonMainApi"(libs.androidx.annotations)
    "commonMainApi"(libs.androidx.compose.runtime)
    "commonMainApi"(libs.androidx.compose.runtime.retain)
    "commonMainApi"(libs.androidx.compose.runtime.saveable)
    "commonMainApi"(libs.androidx.lifecycle.common)
    "commonMainApi"(libs.androidx.navigation.event)
    "commonMainApi"(libs.androidx.viewmodel.savedstate)
    "commonMainApi"(libs.androidx.savedstate)
    "commonMainApi"(libs.jetbrains.compose.ui)
    "commonMainApi"(libs.jetbrains.compose.foundation)
    "commonMainApi"(libs.uri)

    "androidMainApi"(libs.androidx.activity)
    "androidMainApi"(libs.androidx.core)
    "androidMainApi"(libs.androidx.compose.ui)
    "androidMainApi"(libs.coroutines.core)

    "androidMainImplementation"(libs.androidx.activity.compose)
    "androidMainImplementation"(libs.androidx.compose.foundation)
    "androidMainImplementation"(libs.androidx.compose.ui)
    "androidMainImplementation"(libs.androidx.lifecycle.common.android)
    "androidMainImplementation"(libs.androidx.lifecycle.runtime.android)
    "androidMainImplementation"(libs.androidx.lifecycle.compose.android)
    "androidMainImplementation"(libs.androidx.navigation.event.android)
    "androidMainImplementation"(libs.androidx.navigation.event.compose.android)
    "androidMainImplementation"(libs.kotlin.parcelize)

    "jvmMainApi"(libs.androidx.viewmodel.savedstate.desktop)
    "jvmMainApi"(libs.androidx.savedstate.desktop)
    "jvmMainApi"(libs.jetbrains.compose.ui.desktop)
    "jvmMainImplementation"(libs.androidx.navigation.event.desktop)
    "jvmMainImplementation"(libs.androidx.navigation.event.compose.desktop)
    "jvmMainImplementation"(libs.jetbrains.compose.animation.core.desktop)
    "jvmMainImplementation"(libs.jetbrains.compose.foundation.layout.desktop)
    "jvmMainImplementation"(libs.jetbrains.compose.ui.unit.desktop)
    "jvmMainImplementation"(libs.jetbrains.compose.ui.util.desktop)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.turbine)
    "androidHostTestImplementation"(libs.androidx.lifecycle.testing)
    "androidHostTestImplementation"(libs.androidx.lifecycle.common.android)
    "androidHostTestImplementation"(libs.androidx.lifecycle.runtime.android)
    "androidHostTestImplementation"(libs.coroutines.test)
    "androidHostTestImplementation"(projects.navigationTesting)

    "jvmTestImplementation"(libs.junit)
    "jvmTestImplementation"(libs.truth)
    "jvmTestImplementation"(libs.androidx.viewmodel.savedstate.desktop)
    "jvmTestImplementation"(libs.androidx.savedstate.desktop)
}
