plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

android {
    namespace = "com.freeletics.mad.whetstone.compose"
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
            optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
        }
    }
}

dependencies {
    api(project(":whetstone:runtime"))

    implementation(project(":state-machine"))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.savedstate)
    implementation(libs.androidx.viewmodel.compose)
}
