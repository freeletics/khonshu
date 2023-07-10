pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.freeletics.gradle.settings").version("0.3.8")
}

rootProject.name = "simple-sample"

configure<com.freeletics.gradle.plugin.SettingsExtension> {
    includeBuild("../..") {
        dependencySubstitution {
            substitute(module("com.freeletics.khonshu:state-machine"))
                .using(project(":state-machine"))
            substitute(module("com.freeletics.khonshu:state-machine-testing"))
                .using(project(":state-machine-testing"))
            substitute(module("com.freeletics.khonshu:text-resource"))
                .using(project(":text-resource"))
            substitute(module("com.freeletics.khonshu:navigation"))
                .using(project(":navigation"))
            substitute(module("com.freeletics.khonshu:navigation-androidx"))
                .using(project(":navigation-androidx"))
            substitute(module("com.freeletics.khonshu:navigation-compose"))
                .using(project(":navigation-compose"))
            substitute(module("com.freeletics.khonshu:navigation-experimental"))
                .using(project(":navigation-experimental"))
            substitute(module("com.freeletics.khonshu:navigation-fragment"))
                .using(project(":navigation-fragment"))
            substitute(module("com.freeletics.khonshu:navigation-testing"))
                .using(project(":navigation-testing"))
            substitute(module("com.freeletics.khonshu:codegen"))
                .using(project(":codegen"))
            substitute(module("com.freeletics.khonshu:codegen-compose"))
                .using(project(":codegen-compose"))
            substitute(module("com.freeletics.khonshu:codegen-fragment"))
                .using(project(":codegen-fragment"))
            substitute(module("com.freeletics.khonshu:codegen-scope"))
                .using(project(":codegen-scope"))
            substitute(module("com.freeletics.khonshu:codegen-compiler"))
                .using(project(":codegen-compiler"))
        }
    }
}
