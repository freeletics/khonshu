plugins {
    id("com.freeletics.gradle.android")
}

dependencies {
    testImplementation(projects.codegenCompiler)
    testImplementation(projects.codegen)
    testImplementation(projects.navigation)
    testImplementation(projects.stateMachine)
    testImplementation(libs.androidx.compose.runtime)
    testImplementation(libs.coroutines.core)

    testImplementation(libs.kotlinpoet)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.compose.compiler)
    testImplementation(testFixtures(projects.codegenCompiler))
    testImplementation(libs.flowredux2)
}
