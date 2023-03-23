plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
    optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
}

dependencies {
    api(projects.whetstone.navigation)
    api(projects.whetstone.runtimeFragment)
    api(projects.navigator.navigatorRuntime)
    api(libs.androidx.fragment)

    implementation(projects.stateMachine.runtime)
    implementation(projects.whetstone.scope)
    implementation(projects.whetstone.runtime)
    implementation(projects.navigator.navigatorRuntimeFragment)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.savedstate)

    compileOnly(libs.renderer)
}
