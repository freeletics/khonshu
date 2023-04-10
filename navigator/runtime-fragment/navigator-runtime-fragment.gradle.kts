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
}

dependencies {
    api(projects.navigator.runtime)
    api(libs.androidx.fragment)
    api(libs.androidx.navigation.fragment)

    implementation(projects.navigator.androidxNav)
    implementation(libs.coroutines.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.runtime)
}
