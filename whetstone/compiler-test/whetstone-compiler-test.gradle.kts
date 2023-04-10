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
    testImplementation(projects.whetstone.compiler)
    testImplementation(projects.whetstone.scope)
    testImplementation(projects.whetstone.runtime)
    testImplementation(projects.whetstone.runtimeCompose)
    testImplementation(projects.whetstone.runtimeFragment)
    testImplementation(projects.whetstone.navigation)
    testImplementation(projects.whetstone.navigationCompose)
    testImplementation(projects.whetstone.navigationFragment)
    testImplementation(projects.navigator.runtime)
    testImplementation(projects.navigator.runtimeFragment)
    testImplementation(projects.navigator.runtimeCompose)
    testImplementation(projects.stateMachine.runtime)
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
    testImplementation(libs.anvil.compiler.utils)
    testImplementation(testFixtures(libs.anvil.compiler.utils))
}

// exclude dependency from renderer connect, we include the local module instead
configurations.configureEach {
    resolutionStrategy.dependencySubstitution.run {
        exclude("com.freeletics.mad", "state-machine")
    }
}
