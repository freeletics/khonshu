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
        val component = ComponentGenerator(data)
        val componentProvider = NavDestinationComponentProviderGenerator(data)
        val module = ComponentModuleGenerator(data)
        val destinationComposable = NavDestinationComposableGenerator(data)
        val componentComposable = ComponentComposableGenerator(data)
        val destinationModule = NavDestinationModuleGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(component.generate())
            .addType(componentProvider.generate())
            .addType(module.generate())
            .addFunction(destinationComposable.generate())
            .addFunction(componentComposable.generate())
            .addType(destinationModule.generate())
            .build()
    }

    public fun generate(data: NavHostActivityData): FileSpec {
        val component = ComponentGenerator(data)
        val componentProvider = ActivityComponentProviderGenerator(data)
        val module = ComponentModuleGenerator(data)
        val activityModule = ActivityModuleGenerator(data)
        val activity = ActivityGenerator(data)
        val componentComposable = ComponentComposableGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(component.generate())
            .addType(componentProvider.generate())
            .addType(module.generate())
            .addType(activityModule.generate())
            .addType(activity.generate())
            .addFunction(componentComposable.generate())
            .build()
    }
}
