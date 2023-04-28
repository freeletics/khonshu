package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.BaseData
import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.common.ComponentGenerator
import com.freeletics.mad.whetstone.codegen.common.ComposeGenerator
import com.freeletics.mad.whetstone.codegen.common.ModuleGenerator
import com.freeletics.mad.whetstone.codegen.compose.ComposeScreenGenerator
import com.freeletics.mad.whetstone.codegen.fragment.ComposeFragmentGenerator
import com.freeletics.mad.whetstone.codegen.fragment.RendererFragmentGenerator
import com.freeletics.mad.whetstone.codegen.nav.DestinationComponentGenerator
import com.freeletics.mad.whetstone.codegen.nav.NavDestinationModuleGenerator
import com.freeletics.mad.whetstone.codegen.nav.NavEntryComponentGetterGenerator
import com.squareup.kotlinpoet.FileSpec

public class FileGenerator {

    public fun generate(data: BaseData): FileSpec {
        return when (data) {
            is ComposeFragmentData -> generate(data)
            is ComposeScreenData -> generate(data)
            is RendererFragmentData -> generate(data)
            is NavEntryData -> throw IllegalArgumentException("Can't generate file for NavEntryData")
        }
    }

    public fun generate(data: ComposeScreenData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val composeScreenGenerator = ComposeScreenGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(componentGenerator.generate())
            .addType(moduleGenerator.generate())
            .addFunction(composeScreenGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationTypes(data)
            .addNavEntryTypes(data.navEntryData)
            .build()
    }

    public fun generate(data: ComposeFragmentData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val composeFragmentGenerator = ComposeFragmentGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(componentGenerator.generate())
            .addType(moduleGenerator.generate())
            .addType(composeFragmentGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationTypes(data)
            .addNavEntryTypes(data.navEntryData)
            .build()
    }

    public fun generate(data: RendererFragmentData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val rendererFragmentGenerator = RendererFragmentGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(componentGenerator.generate())
            .addType(moduleGenerator.generate())
            .addType(rendererFragmentGenerator.generate())
            .addNavDestinationTypes(data)
            .addNavEntryTypes(data.navEntryData)
            .build()
    }

    private fun FileSpec.Builder.addNavDestinationTypes(data: BaseData) = apply {
        if (data.navigation != null) {
            val navDestinationGenerator = NavDestinationModuleGenerator(data)
            addType(navDestinationGenerator.generate())
        }
    }

    private fun FileSpec.Builder.addNavEntryTypes(data: NavEntryData?) = apply {
        if (data != null) {
            val componentGenerator = ComponentGenerator(data)
            val moduleGenerator = ModuleGenerator(data)
            val componentGetterGenerator = NavEntryComponentGetterGenerator(data)
            val destinationComponentGenerator = DestinationComponentGenerator(data)

            addType(componentGenerator.generate())
            addType(moduleGenerator.generate())
            addType(componentGetterGenerator.generate())
            addType(destinationComponentGenerator.generate())
        }
    }
}
