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

// exclude external dependency on state machine connect, we include the local module instead
configurations.configureEach {
    resolutionStrategy.dependencySubstitution.run {
        substitute(module("com.freeletics.mad:state-machine"))
            .using(project(projects.stateMachine.dependencyProject.path))
    }
}
