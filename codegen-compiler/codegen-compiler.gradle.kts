plugins {
    alias(libs.plugins.fgp.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fgp.publish)
    id("java-test-fixtures")
}

freeletics {
    optIn(
        "com.squareup.anvil.annotations.ExperimentalAnvilApi",
        "com.google.devtools.ksp.KspExperimental",
        "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
    )
}

dependencies {
    api(libs.kotlin.compiler)
    api(libs.anvil.compiler.api)
    api(libs.ksp.api)
    api(libs.kotlinpoet)
    implementation(libs.anvil.annotations)
    implementation(libs.anvil.annotations.optional)
    implementation(libs.anvil.compiler.utils)
    implementation(libs.kotlinpoet.ksp)
    implementation(projects.codegen)
    implementation(projects.navigation)

    compileOnly(libs.auto.service.annotations)
    ksp(libs.auto.service.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.flowredux)
    // TODO https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/972
    testImplementation(projects.codegenCompiler)

    testFixturesApi(libs.kotlin.compile.testing)
    testFixturesImplementation(libs.anvil.annotations)
    testFixturesImplementation(libs.anvil.compiler)
    testFixturesImplementation(testFixtures(libs.anvil.compiler.utils))
    testFixturesImplementation(libs.kotlin.compile.testing.ksp)
    testFixturesImplementation(libs.dagger.compiler)
    testFixturesImplementation(libs.auto.value)
}
