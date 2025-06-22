plugins {
    id("com.freeletics.gradle.multiplatform")
    id("com.freeletics.gradle.publish.oss")
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

    multiplatform {
        addJvmTarget()
        addAndroidTarget {
            enableParcelize()
            consumerProguardFiles("navigation.pro")
        }
    }
}

dependencies {
    "commonMainApi"(libs.androidx.compose.runtime)
    "commonMainApi"(libs.collections.immutable)
    "commonMainApi"(libs.uri)

    "androidMainApi"(libs.androidx.compose.foundation)
    "androidMainApi"(libs.androidx.activity)
    "androidMainApi"(libs.androidx.activity.compose)
    "androidMainApi"(libs.androidx.core)
    "androidMainApi"(libs.androidx.lifecycle.common)
    "androidMainApi"(libs.androidx.viewmodel.compose)
    "androidMainApi"(libs.androidx.viewmodel.savedstate)
    "androidMainApi"(libs.androidx.compose.ui)
    "androidMainApi"(libs.coroutines.core)

    "androidMainImplementation"(libs.androidx.lifecycle.runtime)
    "androidMainImplementation"(libs.kotlin.parcelize)

    "androidHostTestImplementation"(libs.junit)
    "androidHostTestImplementation"(libs.truth)
    "androidHostTestImplementation"(libs.turbine)
    "androidHostTestImplementation"(libs.androidx.lifecycle.testing)
    "androidHostTestImplementation"(libs.coroutines.test)
}
