plugins {
    alias(libs.plugins.fgp.feature).apply(false)
    alias(libs.plugins.fgp.nav).apply(false)
    alias(libs.plugins.fgp.app).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.dependency.analysis).apply(false)
    alias(libs.plugins.kotlin).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.metro).apply(false)
    alias(libs.plugins.khonshu.deeplinks).apply(false)

    alias(libs.plugins.fgp.root)
}
