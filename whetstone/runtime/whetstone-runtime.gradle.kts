plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
}


dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.viewmodel)
    api(libs.androidx.viewmodel.savedstate)
    api(projects.stateMachine.runtime)
    api(projects.whetstone.scope)

    implementation(libs.coroutines.core)
}