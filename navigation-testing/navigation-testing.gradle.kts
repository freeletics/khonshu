plugins {
    id("com.freeletics.gradle.multiplatform")
}

freeletics {
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi")

    useSerialization()
    usePoko()
    enableOssPublishing()

    multiplatform {
        addJvmTarget()
        addAndroidTarget()
    }
}

@OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
kotlin {
    abiValidation {
        filters {
            excluded {
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
    "commonMainApi"(projects.navigation)
    "commonMainApi"(libs.kotlin.test)

    "commonMainImplementation"(libs.toml)

    "androidMainApi"(libs.coroutines.core)
    "androidMainApi"(libs.turbine)

    "androidMainImplementation"(libs.androidx.activity)
    "androidMainImplementation"(libs.truth)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
}
