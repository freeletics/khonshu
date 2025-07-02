package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavDestinationData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.squareup.kotlinpoet.FileSpec

public class FileGenerator {
    public fun generate(data: BaseData): FileSpec {
        return when (data) {
            is NavDestinationData -> generate(data)
            is NavHostActivityData -> generate(data)
        }
    }

    public fun generate(data: NavDestinationData): FileSpec {
        val graph = GraphGenerator(data)
        val graphProvider = NavDestinationGraphProviderGenerator(data)
        val destinationComposable = NavDestinationComposableGenerator(data)
        val graphComposable = GraphComposableGenerator(data)
        val destinationModule = NavDestinationGraphGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(graph.generate())
            .addType(graphProvider.generate())
            .addFunction(destinationComposable.generate())
            .addFunction(graphComposable.generate())
            .addType(destinationModule.generate())
            .build()
    }

    public fun generate(data: NavHostActivityData): FileSpec {
        val graph = GraphGenerator(data)
        val graphProvider = ActivityGraphProviderGenerator(data)
        val activityModule = ActivityGraphGenerator(data)
        val activity = ActivityGenerator(data)
        val graphComposable = GraphComposableGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(graph.generate())
            .addType(graphProvider.generate())
            .addType(activityModule.generate())
            .addType(activity.generate())
            .addFunction(graphComposable.generate())
            .build()
    }
}
