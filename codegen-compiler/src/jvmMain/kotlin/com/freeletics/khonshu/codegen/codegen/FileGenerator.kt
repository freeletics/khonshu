package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.DestinationData
import com.freeletics.khonshu.codegen.HostActivityData
import com.freeletics.khonshu.codegen.HostWindowData
import com.squareup.kotlinpoet.FileSpec

public class FileGenerator {
    public fun generate(data: BaseData): FileSpec {
        return when (data) {
            is DestinationData -> generate(data)
            is HostActivityData -> generate(data)
            is HostWindowData -> generate(data)
        }
    }

    public fun generate(data: DestinationData): FileSpec {
        val graph = GraphGenerator(data)
        val graphProvider = NavDestinationGraphProviderGenerator(data)
        val graphComposable = GraphComposableGenerator(data)
        val destinationComposable = DestinationComposableGenerator(data)
        val destinationGraphContribution = DestinationGraphContributionGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(graph.generate())
            .addType(graphProvider.generate())
            .addFunction(destinationComposable.generate())
            .addFunction(graphComposable.generate())
            .addType(destinationGraphContribution.generate())
            .build()
    }

    public fun generate(data: HostActivityData): FileSpec {
        val graph = GraphGenerator(data)
        val graphProvider = HostGraphProviderGenerator(data)
        val graphComposable = GraphComposableGenerator(data)
        val hostGraphContribution = HostGraphContributionGenerator(data)
        val activity = HostActivityGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(graph.generate())
            .addType(graphProvider.generate())
            .addType(hostGraphContribution.generate())
            .addType(activity.generate())
            .addFunction(graphComposable.generate())
            .build()
    }

    public fun generate(data: HostWindowData): FileSpec {
        val graph = GraphGenerator(data)
        val graphProvider = HostGraphProviderGenerator(data)
        val activityModule = HostGraphContributionGenerator(data)
        val window = HostWindowGenerator(data)
        val graphComposable = GraphComposableGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(graph.generate())
            .addType(graphProvider.generate())
            .addType(activityModule.generate())
            .addType(window.generate())
            .addFunction(graphComposable.generate())
            .build()
    }
}
