import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask

plugins {
    alias(libs.plugins.fgp.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn(
        "com.squareup.anvil.annotations.ExperimentalAnvilApi",
        "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
    )
}

dependencies {
    api(libs.kotlin.compiler)
    api(libs.anvil.compiler.api)
    api(libs.kotlinpoet)
    implementation(libs.anvil.annotations)
    implementation(libs.anvil.compiler.utils)

    compileOnly(libs.auto.service.annotations)
    kapt(libs.auto.service.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.flowredux)
    testImplementation(testFixtures(libs.anvil.compiler.utils))
}

// TODO remove when Kotlin 1.8.20 is out and FGP was updated
tasks.withType(KaptGenerateStubsTask::class.java).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.target.get()))
    }
}
