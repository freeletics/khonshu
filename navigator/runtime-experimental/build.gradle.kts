plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

android {
    namespace = "com.freeletics.mad.navigator.runtime.compose"
    compileSdk = libs.versions.android.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.get().toInt()
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    // still needed for Android projects despite toolchain
    compileOptions {
        sourceCompatibility(JavaVersion.toVersion(libs.versions.java.get()))
        targetCompatibility(JavaVersion.toVersion(libs.versions.java.get()))
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
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
        }
    }
}

dependencies {
    api(projects.navigator.navigatorRuntime)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)

    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.viewmodel.savedstate)
}
