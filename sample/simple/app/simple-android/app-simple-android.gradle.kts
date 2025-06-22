plugins {
    id("com.freeletics.gradle.app.android")
}

freeletics {
    useMetro()
    useCompose()

    app {
        applicationId("com.freeletics.khonshu.sample.simple")
        minify()
    }
}

dependencies {
    implementation(projects.app.simple)
    implementation(libs.khonshu.codegen.runtime)
}
