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
    testImplementation(testFixtures(projects.codegenCompiler))
}
