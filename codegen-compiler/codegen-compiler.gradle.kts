plugins {
    id("com.freeletics.gradle.multiplatform")
    id("com.freeletics.gradle.publish.oss")
    id("java-test-fixtures")
}

freeletics {
    optIn("com.google.devtools.ksp.KspExperimental")

    multiplatform {
        addJvmTarget()
    }
}

dependencies {
    "jvmMainApi"(libs.ksp.api)
    "jvmMainApi"(libs.kotlinpoet)
    "jvmMainImplementation"(libs.kotlinpoet.ksp)
    "jvmMainImplementation"(projects.codegen)

    "jvmTestImplementation"(libs.junit)
    "jvmTestImplementation"(libs.truth)
    "jvmTestImplementation"(libs.kotlin.compile.testing)
    "jvmTestImplementation"(libs.coroutines.core)
    "jvmTestImplementation"(libs.flowredux)
    // TODO https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/972
    "jvmTestImplementation"(projects.codegenCompiler)

    "jvmTestFixturesApi"(libs.kotlin.compile.testing)
    "jvmTestFixturesApi"(libs.kotlin.compiler)
    "jvmTestFixturesImplementation"(libs.kotlin.compile.testing.ksp)
    // explicitly depend on ksp to force the version to a newer one than compile testing uses
    "jvmTestFixturesRuntimeOnly"(libs.ksp)
    "jvmTestFixturesRuntimeOnly"(libs.ksp.deps)
    "jvmTestFixturesRuntimeOnly"(libs.ksp.embeddable)
}
