import com.freeletics.gradle.plugin.FreeleticsAndroidExtension

plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.poko)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationApi")

    multiplatform {
        addJvmTarget()
        addAndroidTarget(publish = true)
    }

    extensions.configure(FreeleticsAndroidExtension::class) {
        enableParcelize()
        consumerProguardFiles("navigation.pro")
    }
}

dependencies {
    "androidMainApi"(libs.androidx.activity)
    "androidMainApi"(libs.androidx.core)
    "androidMainApi"(libs.androidx.lifecycle.common)
    "androidMainApi"(libs.androidx.viewmodel.savedstate)
    "androidMainApi"(libs.coroutines.core)
    "androidMainApi"(libs.uri)

    "androidMainImplementation"(libs.androidx.lifecycle.runtime)
    "androidMainImplementation"(libs.kotlin.parcelize)

    "androidUnitTestImplementation"(libs.junit)
    "androidUnitTestImplementation"(libs.truth)
    "androidUnitTestImplementation"(libs.turbine)
    "androidUnitTestImplementation"(libs.androidx.lifecycle.testing)
    "androidUnitTestImplementation"(libs.coroutines.test)
}
