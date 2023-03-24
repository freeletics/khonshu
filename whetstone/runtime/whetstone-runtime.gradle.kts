plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
    enableCompose()
}


dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.viewmodel)
    api(libs.androidx.viewmodel.savedstate)
    api(projects.stateMachine.runtime)
    api(projects.whetstone.scope)

    implementation(libs.coroutines.core)
}
