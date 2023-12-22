import com.freeletics.gradle.plugin.FreeleticsAndroidExtension

plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.poko)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationApi")

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
    "commonMainApi"(libs.uri)

    "androidMainApi"(libs.androidx.activity)
    "androidMainApi"(libs.androidx.activity.compose)
    "androidMainApi"(libs.androidx.core)
    "androidMainApi"(libs.androidx.lifecycle.common)
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
