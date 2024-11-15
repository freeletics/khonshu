plugins {
    id("com.freeletics.gradle.jvm")
    alias(libs.plugins.ksp)
    id("com.freeletics.gradle.publish.oss")
    id("java-test-fixtures")
}

freeletics {
    optIn("com.google.devtools.ksp.KspExperimental")
}

dependencies {
    api(libs.ksp.api)
    api(libs.kotlinpoet)
    implementation(libs.anvil.annotations)
    implementation(libs.anvil.annotations.optional)
    implementation(libs.kotlinpoet.ksp)
    implementation(projects.codegen)

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
    testFixturesApi(libs.kotlin.compiler)
    testFixturesImplementation(libs.kotlin.compile.testing.ksp)
    // explicitly depend on ksp to force the version to a newer one than compile testing uses
    testFixturesRuntimeOnly(libs.ksp)
    testFixturesRuntimeOnly(libs.ksp.deps)
    testFixturesRuntimeOnly(libs.ksp.embeddable)
}
