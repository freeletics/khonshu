plugins {
    id("com.freeletics.gradle.app.android")
    id("com.freeletics.khonshu.deeplinks")
}

freeletics {
    useMetro()
    useCompose()

    app {
        applicationId("com.freeletics.khonshu.sample.simple")
        minify()
    }
}

deepLinks {
    deepLinkDefinitionsFile = file("src/test/resources/deeplinks.toml")
}

dependencies {
    implementation(projects.app.simple)
    implementation(libs.khonshu.codegen.runtime)
}
