plugins {
    id("com.freeletics.gradle.app")
}

freeletics {
    useMetro()
    useCompose()
}

dependencies {
    commonMainImplementation(libs.androidx.lifecycle.viewmodel.savedstate)
    commonMainImplementation(libs.khonshu.navigation)
    commonMainImplementation(projects.feature.bottomSheet.implementation)
    commonMainImplementation(projects.feature.bottomSheet.nav)
    commonMainImplementation(projects.feature.dialog.implementation)
    commonMainImplementation(projects.feature.dialog.nav)
    commonMainImplementation(projects.feature.main.implementation)
    commonMainImplementation(projects.feature.root.implementation)
    commonMainImplementation(projects.feature.root.nav)
    commonMainImplementation(projects.feature.screen.implementation)
    commonMainImplementation(projects.feature.screen.nav)
    commonMainImplementation(projects.feature.screenWithResult.implementation)
    commonMainImplementation(projects.feature.screenWithResult.nav)
    commonMainImplementation(projects.feature.newRoot.implementation)
    commonMainImplementation(projects.feature.newRoot.nav)
}
