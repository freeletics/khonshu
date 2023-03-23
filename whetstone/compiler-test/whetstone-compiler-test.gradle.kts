plugins {
    alias(libs.plugins.fgp.android)
}

freeletics {
    explicitApi()
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
    testImplementation(projects.navigator.navigatorRuntime)
    testImplementation(projects.navigator.navigatorRuntimeFragment)
    testImplementation(projects.navigator.navigatorRuntimeCompose)
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
    testImplementation(libs.kotlin.compiler) {
        version {
            strictly(libs.versions.kotlin.asProvider().get())
        }
    }
}

// exclude dependency from renderer connect, we include the local module instead
configurations.configureEach {
    resolutionStrategy.dependencySubstitution.run {
        exclude("com.freeletics.mad", "state-machine")
    }
}
