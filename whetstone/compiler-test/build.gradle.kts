plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.freeletics.mad.whetstone.test"
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
}

dependencies {
    testImplementation(project(":whetstone:compiler"))
    testImplementation(project(":whetstone:runtime"))
    testImplementation(project(":whetstone:runtime-compose"))
    testImplementation(project(":whetstone:runtime-fragment"))
    testImplementation(project(":whetstone:navigation"))
    testImplementation(project(":whetstone:navigation-compose"))
    testImplementation(project(":whetstone:navigation-fragment"))
    testImplementation(project(":navigator:navigator-runtime"))
    testImplementation(project(":navigator:navigator-runtime-fragment"))
    testImplementation(project(":navigator:navigator-runtime-compose"))
    testImplementation(project(":state-machine"))
    testImplementation(libs.androidx.compose.runtime)
    testImplementation(libs.androidx.viewbinding)
    testImplementation(libs.renderer)
    testImplementation(libs.renderer.connect)
    testImplementation(libs.coroutines.core)

    testImplementation(libs.kotlinpoet)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.androidx.compose.compiler)
    testImplementation(libs.anvil.annotations)
    testImplementation(libs.anvil.compiler.utils)
    testImplementation(testFixtures(libs.anvil.compiler.utils))
    testImplementation(libs.kotlin.compiler) { setForce(true) }
}

configurations.all {
    resolutionStrategy.dependencySubstitution.run {
        substitute(module("com.freeletics.mad:state-machine")).using(project(":state-machine"))
    }
}
