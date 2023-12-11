package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.codegen.common.ComponentGenerator
import com.freeletics.khonshu.codegen.codegen.common.ComponentProviderGenerator
import com.freeletics.khonshu.codegen.codegen.common.InnerComposableGenerator
import com.freeletics.khonshu.codegen.codegen.common.ModuleGenerator
import com.freeletics.khonshu.codegen.codegen.common.NavDestinationModuleGenerator
import com.freeletics.khonshu.codegen.codegen.compose.ActivityGenerator
import com.freeletics.khonshu.codegen.codegen.compose.ActivityModuleGenerator
import com.freeletics.khonshu.codegen.codegen.compose.OuterComposableGenerator
import com.squareup.kotlinpoet.FileSpec

public class FileGenerator {

    public fun generate(data: BaseData): FileSpec {
        return when (data) {
            is ComposeScreenData -> generate(data)
            is NavHostActivityData -> generate(data)
        }
    }

    public fun generate(data: ComposeScreenData): FileSpec {
        val component = ComponentGenerator(data)
        val module = ModuleGenerator(data)
        val outerComposable = OuterComposableGenerator(data)
        val innerComposable = InnerComposableGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(component.generate())
            .addComponentProviderType(data)
            .addType(module.generate())
            .addFunction(outerComposable.generate())
            .addFunction(innerComposable.generate())
            .addNavDestinationTypes(data)
            .build()
    }

    public fun generate(data: NavHostActivityData): FileSpec {
        val component = ComponentGenerator(data)
        val module = ModuleGenerator(data)
        val activityModule = ActivityModuleGenerator(data)
        val activity = ActivityGenerator(data)
        val innerComposable = InnerComposableGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(component.generate())
            .addType(module.generate())
            .addType(activityModule.generate())
            .addType(activity.generate())
            .addFunction(innerComposable.generate())
            .build()
    }

    private fun FileSpec.Builder.addComponentProviderType(data: BaseData) = apply {
        if (data.navigation != null) {
            val componentProvider = ComponentProviderGenerator(data)
            addType(componentProvider.generate())
        }
    }

    private fun FileSpec.Builder.addNavDestinationTypes(data: BaseData) = apply {
        if (data.navigation != null) {
            val navDestinationModule = NavDestinationModuleGenerator(data)
            addType(navDestinationModule.generate())
        }
    }
}
