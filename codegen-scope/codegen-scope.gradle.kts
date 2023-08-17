plugins {
    alias(libs.plugins.fgp.jvm)
    alias(libs.plugins.fgp.publish)
}

freeletics {
}

dependencies {
    api(projects.codegen)
}
