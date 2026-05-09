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
    "androidMainImplementation"(libs.kotlin.parcelize)
    "androidMainImplementation"(libs.androidx.lifecycle.compose)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.turbine)
    "androidHostTestImplementation"(libs.androidx.lifecycle.testing)
    "androidHostTestImplementation"(libs.coroutines.test)
    "androidHostTestImplementation"(projects.navigationTesting)
    "androidHostTestImplementation"(libs.androidx.lifecycle.compose)

    "jvmTestImplementation"(libs.junit)
    "jvmTestImplementation"(libs.truth)
}
