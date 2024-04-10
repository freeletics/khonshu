import com.android.build.api.dsl.CommonExtension
import com.freeletics.gradle.plugin.FreeleticsAndroidExtension

plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.poko)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    optIn(
        "com.freeletics.khonshu.navigation.internal.InternalNavigationApi",
        "com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi",
        "com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi",
    )

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

extensions.configure(CommonExtension::class.java) {
    lint {
        disable.add("UnsafeOptInUsageError")
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
