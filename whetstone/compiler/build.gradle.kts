plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.publish)
}

kotlin {
    explicitApi()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.target.get().toInt()))
    }
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
    testImplementation(libs.flowredux)
    testImplementation(testFixtures(libs.anvil.compiler.utils))
}
