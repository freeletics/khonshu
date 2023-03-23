plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.anvil).apply(false)
    alias(libs.plugins.dependency.analysis).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)

    alias(libs.plugins.fl.root)
}
