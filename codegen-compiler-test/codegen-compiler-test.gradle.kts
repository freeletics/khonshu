plugins {
    alias(libs.plugins.fgp.android)
}

freeletics {
    explicitApi()
    optIn(
        "com.squareup.anvil.annotations.ExperimentalAnvilApi",
        "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
    )
}

dependencies {
    testImplementation(projects.codegenCompiler)
    testImplementation(projects.codegenScope)
    testImplementation(projects.codegen)
    testImplementation(projects.codegenCompose)
    testImplementation(projects.codegenFragment)
    testImplementation(projects.navigation)
    testImplementation(projects.navigationFragment)
    testImplementation(projects.navigationCompose)
    testImplementation(projects.stateMachine)
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
    testImplementation(libs.anvil.annotations)
    testImplementation(testFixtures(projects.codegenCompiler))
    testImplementation(testFixtures(libs.anvil.compiler.utils))
}

// exclude external dependency on state machine connect, we include the local module instead
configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("com.freeletics.khonshu:state-machine"))
            .using(project(projects.stateMachine.dependencyProject.path))
    }
}
