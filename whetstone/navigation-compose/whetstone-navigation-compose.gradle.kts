plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
    optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
    enableCompose()
}

dependencies {
    api(projects.whetstone.navigation)
    api(projects.whetstone.runtimeCompose)
    api(projects.navigator.navigatorRuntime)

    implementation(projects.stateMachine.runtime)
    implementation(projects.whetstone.scope)
    implementation(projects.whetstone.runtime)
    implementation(projects.navigator.navigatorRuntimeCompose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.savedstate)
}
