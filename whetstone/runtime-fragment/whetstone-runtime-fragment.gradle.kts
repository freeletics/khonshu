plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.whetstone.internal.InternalWhetstoneApi")
    enableCompose()
}

dependencies {
    api(projects.whetstone.runtime)
    api(libs.androidx.fragment)

    implementation(projects.stateMachine.runtime)
    implementation(projects.whetstone.scope)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewmodel.savedstate)

    compileOnly(libs.renderer)
}
