plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

android {
    namespace = "com.freeletics.mad.whetstone.navigation.fragment"
    compileSdk = libs.versions.android.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.get().toInt()
    }

    buildFeatures {
        buildConfig = false
    }

    // still needed for Android projects despite toolchain
    compileOptions {
        sourceCompatibility(JavaVersion.toVersion(libs.versions.java.get()))
        targetCompatibility(JavaVersion.toVersion(libs.versions.java.get()))
    }
}

// workaround for https://youtrack.jetbrains.com/issue/KT-37652
android.kotlinOptions.freeCompilerArgs += "-Xexplicit-api=strict"

kotlin {
    explicitApi()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }

    sourceSets.all {
        languageSettings {
            optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
            optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
        }
    }
}

dependencies {
    api(projects.whetstone.navigation)
    api(projects.whetstone.runtimeFragment)
    api(projects.navigator.navigatorRuntime)
    api(libs.androidx.fragment)

    implementation(projects.stateMachine)
    implementation(projects.whetstone.scope)
    implementation(projects.whetstone.runtime)
    implementation(projects.navigator.navigatorRuntimeFragment)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.savedstate)

    compileOnly(libs.renderer)
}
