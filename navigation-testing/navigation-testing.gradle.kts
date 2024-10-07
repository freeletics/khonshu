import com.android.build.api.dsl.CommonExtension

plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.fgp.publish)
    alias(libs.plugins.poko)
}

freeletics {
    optIn("com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi")
    useSerialization()

    multiplatform {
        addJvmTarget()
        addAndroidTarget()
    }
}

extensions.configure(CommonExtension::class.java) {
    lint {
        disable.add("UnsafeOptInUsageError")
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
