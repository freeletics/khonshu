plugins {
    alias(libs.plugins.fgp.android)
}

freeletics {
    optIn(
        "com.squareup.anvil.annotations.ExperimentalAnvilApi",
        "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
    )
}

dependencies {
    testImplementation(projects.codegenCompiler)
    testImplementation(projects.codegen)
    testImplementation(projects.navigation)
    testImplementation(projects.stateMachine)
    testImplementation(libs.anvil.annotations)
    testImplementation(libs.anvil.annotations.optional)
    testImplementation(libs.androidx.compose.runtime)
    testImplementation(libs.coroutines.core)

    testImplementation(libs.kotlinpoet)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.compose.compiler)
    testImplementation(testFixtures(projects.codegenCompiler))
}

// TODO remove when kotlin-compile-testing 0.5.0 is stable
configurations.configureEach {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.asProvider().get()}")
    }
}
