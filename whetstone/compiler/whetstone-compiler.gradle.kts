plugins {
    alias(libs.plugins.fgp.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
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
