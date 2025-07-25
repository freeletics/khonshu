import com.freeletics.gradle.plugin.FreeleticsAndroidExtension

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

    multiplatform {
        addJvmTarget()
        addAndroidTarget()
    }

    useCompose()

    extensions.configure(FreeleticsAndroidExtension::class) {
        enableParcelize()
        consumerProguardFiles("navigation.pro")
    }
}

dependencies {
    "commonMainApi"(libs.jetbrains.compose.runtime)
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

    "androidUnitTestImplementation"(libs.junit)
    "androidUnitTestImplementation"(libs.truth)
    "androidUnitTestImplementation"(libs.turbine)
    "androidUnitTestImplementation"(libs.androidx.lifecycle.testing)
    "androidUnitTestImplementation"(libs.coroutines.test)
}
