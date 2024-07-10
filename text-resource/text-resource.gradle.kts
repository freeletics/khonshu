import com.freeletics.gradle.plugin.FreeleticsAndroidExtension

plugins {
    alias(libs.plugins.fgp.multiplatform)
    alias(libs.plugins.poko)
    alias(libs.plugins.fgp.publish)
}

freeletics {
    multiplatform {
        addAndroidTarget()
        addIosTargets()
    }

    useCompose()

    extensions.configure(FreeleticsAndroidExtension::class.java) {
        enableParcelize()
    }
}

dependencies {
    "commonMainApi"(libs.jetbrains.compose.runtime)
    "commonMainApi"(libs.jetbrains.compose.resources)

    "commonMainImplementation"(libs.androidx.annotations)
    "commonMainImplementation"(libs.jetbrains.compose.ui)

    "androidMainImplementation"(libs.kotlin.parcelize)
}
