pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.3.7")
}

rootProject.name = "simple-sample"

configure<com.freeletics.gradle.plugin.SettingsExtension> {
    includeBuild("../..") {
        dependencySubstitution {
            substitute(module("com.freeletics.mad:state-machine"))
                .using(project(":state-machine"))
            substitute(module("com.freeletics.mad:state-machine-testing"))
                .using(project(":state-machine-testing"))
            substitute(module("com.freeletics.mad:text-resource"))
                .using(project(":text-resource"))
            substitute(module("com.freeletics.mad:navigation"))
                .using(project(":navigation"))
            substitute(module("com.freeletics.mad:navigation-androidx"))
                .using(project(":navigation-androidx"))
            substitute(module("com.freeletics.mad:navigation-compose"))
                .using(project(":navigation-compose"))
            substitute(module("com.freeletics.mad:navigation-experimental"))
                .using(project(":navigation-experimental"))
            substitute(module("com.freeletics.mad:navigation-fragment"))
                .using(project(":navigation-fragment"))
            substitute(module("com.freeletics.mad:navigation-testing"))
                .using(project(":navigation-testing"))
            substitute(module("com.freeletics.mad:codegen"))
                .using(project(":codegen"))
            substitute(module("com.freeletics.mad:codegen-compose"))
                .using(project(":codegen-compose"))
            substitute(module("com.freeletics.mad:codegen-fragment"))
                .using(project(":codegen-fragment"))
            substitute(module("com.freeletics.mad:codegen-scope"))
                .using(project(":codegen-scope"))
            substitute(module("com.freeletics.mad:codegen-compiler"))
                .using(project(":codegen-compiler"))
        }
    }
}
