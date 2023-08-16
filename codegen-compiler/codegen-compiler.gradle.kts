plugins {
    alias(libs.plugins.fgp.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.fgp.publish)
    id("java-test-fixtures")
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
    api(projects.codegen)
    implementation(libs.anvil.annotations)
    implementation(libs.anvil.compiler.utils)

    compileOnly(libs.auto.service.annotations)
    kapt(libs.auto.service.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.flowredux)

    testFixturesApi(libs.kotlin.compile.testing)
    testFixturesImplementation(libs.anvil.compiler)
    testFixturesImplementation(testFixtures(libs.anvil.compiler.utils))
    testFixturesImplementation(libs.dagger.compiler)
    testFixturesImplementation(libs.auto.value)
}

// exclude external dependency on state machine connect, we include the local module instead
configurations.configureEach {
    resolutionStrategy.dependencySubstitution.run {
        substitute(module("com.freeletics.khonshu:state-machine"))
            .using(project(projects.stateMachine.dependencyProject.path))
    }
}
