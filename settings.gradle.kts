pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }

    // https://youtrack.jetbrains.com/issue/KT-51379
    // repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

include(":navigator:runtime", ":navigator:testing")
include(":navigator:androidx-nav", ":navigator:runtime-compose", ":navigator:runtime-fragment")
include(":state-machine", ":state-machine:testing")
include(":text-resource")
include(":whetstone:compiler", ":whetstone:compiler-test")
include(":whetstone:runtime", ":whetstone:runtime-compose", ":whetstone:runtime-fragment")
include(":whetstone:navigation", ":whetstone:navigation-compose", ":whetstone:navigation-fragment")

project(":navigator:runtime").setName("navigator-runtime")
project(":navigator:runtime-compose").setName("navigator-runtime-compose")
project(":navigator:runtime-fragment").setName("navigator-runtime-fragment")
