plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

kotlin {
    explicitApi()

    jvm()
    js(IR) { nodejs() }
    iosArm32()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosX86()

    sourceSets.all {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.java.target.get().toInt()))
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.coroutines.core)
            }
        }
    }
}
