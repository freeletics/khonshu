plugins {
    alias(libs.plugins.fgp.app)
}

freeletics {
    useDaggerWithComponent()

    app {
        applicationId("com.freeletics.khonshu.sample.simple")
        minify()
    }
}

// TODO remove when Anvil works with K2
kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9
        progressiveMode = false
    }
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.khonshu.navigation)
    implementation(projects.feature.bottomSheet.implementation)
    implementation(projects.feature.bottomSheet.nav)
    implementation(projects.feature.dialog.implementation)
    implementation(projects.feature.dialog.nav)
    implementation(projects.feature.main)
    implementation(projects.feature.root.implementation)
    implementation(projects.feature.root.nav)
    implementation(projects.feature.screen.implementation)
    implementation(projects.feature.screen.nav)
    implementation(projects.feature.screenWithResult.implementation)
    implementation(projects.feature.screenWithResult.nav)
    implementation(projects.feature.newRoot.implementation)
    implementation(projects.feature.newRoot.nav)
}
