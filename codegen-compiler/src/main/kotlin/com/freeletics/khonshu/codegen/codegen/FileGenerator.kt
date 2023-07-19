package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.RendererFragmentData
import com.freeletics.khonshu.codegen.codegen.common.ComponentGenerator
import com.freeletics.khonshu.codegen.codegen.common.ComponentProviderGenerator
import com.freeletics.khonshu.codegen.codegen.common.ComposeGenerator
import com.freeletics.khonshu.codegen.codegen.common.ModuleGenerator
import com.freeletics.khonshu.codegen.codegen.compose.ComposeScreenGenerator
import com.freeletics.khonshu.codegen.codegen.fragment.ComposeFragmentGenerator
import com.freeletics.khonshu.codegen.codegen.fragment.RendererFragmentGenerator
import com.freeletics.khonshu.codegen.codegen.nav.NavDestinationComponentGenerator
import com.freeletics.khonshu.codegen.codegen.nav.NavDestinationModuleGenerator
import com.squareup.kotlinpoet.FileSpec

public class FileGenerator {

    public fun generate(data: BaseData): FileSpec {
        return when (data) {
            is ComposeFragmentData -> generate(data)
            is ComposeScreenData -> generate(data)
            is RendererFragmentData -> generate(data)
        }
    }

    public fun generate(data: ComposeScreenData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val composeScreenGenerator = ComposeScreenGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(componentGenerator.generate())
            .addComponentProviderType(data)
            .addType(moduleGenerator.generate())
            .addFunction(composeScreenGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationTypes(data)
            .build()
    }

    public fun generate(data: ComposeFragmentData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val composeFragmentGenerator = ComposeFragmentGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(componentGenerator.generate())
            .addComponentProviderType(data)
            .addType(moduleGenerator.generate())
            .addType(composeFragmentGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationTypes(data)
            .build()
    }

    public fun generate(data: RendererFragmentData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val rendererFragmentGenerator = RendererFragmentGenerator(data)

        return FileSpec.builder(data.packageName, "Khonshu${data.baseName}")
            .addType(componentGenerator.generate())
            .addComponentProviderType(data)
            .addType(moduleGenerator.generate())
            .addType(rendererFragmentGenerator.generate())
            .addNavDestinationTypes(data)
            .build()
    }

    private fun FileSpec.Builder.addComponentProviderType(data: BaseData) = apply {
        if (data.navigation != null) {
            val componentProviderGenerator = ComponentProviderGenerator(data)
            addType(componentProviderGenerator.generate())
            val destinationComponentGenerator = NavDestinationComponentGenerator(data)
            addType(destinationComponentGenerator.generate())
        }
    }

    private fun FileSpec.Builder.addNavDestinationTypes(data: BaseData) = apply {
        if (data.navigation != null) {
            val navDestinationGenerator = NavDestinationModuleGenerator(data)
            addType(navDestinationGenerator.generate())
        }
    }
}
