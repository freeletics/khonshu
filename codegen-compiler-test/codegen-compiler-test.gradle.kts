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
    testImplementation(projects.navigationFragment)
    testImplementation(projects.navigationCompose)
    testImplementation(projects.stateMachine)
    testImplementation(libs.anvil.annotations)
    testImplementation(libs.anvil.annotations.optional)
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
    testImplementation(testFixtures(projects.codegenCompiler))
}

// exclude external dependency on state machine connect, we include the local module instead
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.freeletics.khonshu:state-machine"))
            .using(project(projects.stateMachine.dependencyProject.path))
    }
}
