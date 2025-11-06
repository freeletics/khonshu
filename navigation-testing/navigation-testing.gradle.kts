plugins {
    id("com.freeletics.gradle.multiplatform")
    id("com.freeletics.gradle.publish.oss")
}

freeletics {
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi")
    useSerialization()
    usePoko()

    multiplatform {
        addJvmTarget()
        addAndroidTarget {
            enableParcelize()
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
}
