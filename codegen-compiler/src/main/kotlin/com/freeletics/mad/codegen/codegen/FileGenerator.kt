package com.freeletics.mad.codegen.codegen

import com.freeletics.mad.codegen.BaseData
import com.freeletics.mad.codegen.ComposeFragmentData
import com.freeletics.mad.codegen.ComposeScreenData
import com.freeletics.mad.codegen.NavEntryData
import com.freeletics.mad.codegen.RendererFragmentData
import com.freeletics.mad.codegen.codegen.common.ComponentGenerator
import com.freeletics.mad.codegen.codegen.common.ComposeGenerator
import com.freeletics.mad.codegen.codegen.common.ModuleGenerator
import com.freeletics.mad.codegen.codegen.compose.ComposeScreenGenerator
import com.freeletics.mad.codegen.codegen.fragment.ComposeFragmentGenerator
import com.freeletics.mad.codegen.codegen.fragment.RendererFragmentGenerator
import com.freeletics.mad.codegen.codegen.nav.NavDestinationComponentGenerator
import com.freeletics.mad.codegen.codegen.nav.NavDestinationModuleGenerator
import com.freeletics.mad.codegen.codegen.nav.NavEntryComponentGetterGenerator
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

        return FileSpec.builder(data.packageName, "Mad${data.baseName}")
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

        return FileSpec.builder(data.packageName, "Mad${data.baseName}")
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

        return FileSpec.builder(data.packageName, "Mad${data.baseName}")
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
            val destinationComponentGenerator = NavDestinationComponentGenerator(data)

            addType(componentGenerator.generate())
            addType(moduleGenerator.generate())
            addType(componentGetterGenerator.generate())
            addType(destinationComponentGenerator.generate())
        }
    }
}
