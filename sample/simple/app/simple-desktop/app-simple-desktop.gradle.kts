plugins {
    id("com.freeletics.gradle.app.desktop")
}

freeletics {
    useMetro()
    useCompose()
}

dependencies {
    jvmMainImplementation(projects.app.simple)
    jvmMainImplementation(compose.desktop.macos_arm64)
    jvmMainImplementation(libs.coroutines.swing)
    jvmMainImplementation(libs.khonshu.codegen.runtime)
    jvmMainImplementation(projects.feature.main.implementation)
}
