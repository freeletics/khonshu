plugins {
    alias(libs.plugins.fgp.android)
    alias(libs.plugins.fgp.publish)
}

// workaround for Gradle/Studio not being able to differentiate between
// :whetstone:runtime* and :navigator:runtime* if the group is the same
// has no effect on publishing
project.group = "navigator"

freeletics {
    explicitApi()
    optIn("com.freeletics.mad.navigator.internal.InternalNavigatorApi")
    enableCompose()
}

dependencies {
    api(projects.navigator.runtime)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.navigation.runtime)

    implementation(projects.navigator.androidxNav)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.navigation)
}
