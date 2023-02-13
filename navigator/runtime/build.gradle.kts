plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.poko)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

android {
    namespace = "com.freeletics.mad.navigator.runtime"
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
        }
    }
}

dependencies {
    api(libs.androidx.activity)
    api(libs.coroutines.core)
    api(libs.androidx.core)

    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.viewmodel.savedstate)
    implementation(libs.kotlin.parcelize)
    implementation(libs.uri)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.lifecycle.testing)
    testImplementation(libs.coroutines.test)
}
