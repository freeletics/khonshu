package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.BaseData
import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.common.ComponentGenerator
import com.freeletics.mad.whetstone.codegen.common.ComposeGenerator
import com.freeletics.mad.whetstone.codegen.common.ModuleGenerator
import com.freeletics.mad.whetstone.codegen.common.ViewModelGenerator
import com.freeletics.mad.whetstone.codegen.compose.ComposeScreenGenerator
import com.freeletics.mad.whetstone.codegen.fragment.ComposeFragmentGenerator
import com.freeletics.mad.whetstone.codegen.fragment.RendererFragmentGenerator
import com.freeletics.mad.whetstone.codegen.nav.DestinationComponentGenerator
import com.freeletics.mad.whetstone.codegen.nav.NavDestinationModuleGenerator
import com.freeletics.mad.whetstone.codegen.nav.NavEntryComponentGetterGenerator
import com.squareup.kotlinpoet.FileSpec

public class FileGenerator{

    public fun generate(data: ComposeScreenData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val composeScreenGenerator = ComposeScreenGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(componentGenerator.generate())
            .addType(moduleGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addFunction(composeScreenGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationTypes(data)
            .addNavEntryTypes(data.navEntryData)
            .build()
    }

    public fun generate(data: ComposeFragmentData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val composeFragmentGenerator = ComposeFragmentGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(componentGenerator.generate())
            .addType(moduleGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(composeFragmentGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationTypes(data)
            .addNavEntryTypes(data.navEntryData)
            .build()
    }

    public fun generate(data: RendererFragmentData): FileSpec {
        val componentGenerator = ComponentGenerator(data)
        val moduleGenerator = ModuleGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val rendererFragmentGenerator = RendererFragmentGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(componentGenerator.generate())
            .addType(moduleGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(rendererFragmentGenerator.generate())
            .addNavDestinationTypes(data)
            .addNavEntryTypes(data.navEntryData)
            .build()
    }

    private fun FileSpec.Builder.addNavDestinationTypes(data: BaseData) = apply {
        if (data.navigation?.destinationMethod != null) {
            val navDestinationGenerator = NavDestinationModuleGenerator(data)
            addType(navDestinationGenerator.generate())
        }
    }

    private fun FileSpec.Builder.addNavEntryTypes(data: NavEntryData?) = apply {
        if (data != null) {
            val componentGenerator = ComponentGenerator(data)
            val moduleGenerator = ModuleGenerator(data)
            val viewModelGenerator = ViewModelGenerator(data)
            val componentGetterGenerator = NavEntryComponentGetterGenerator(data)
            val destinationComponentGenerator = DestinationComponentGenerator(data)

            addType(componentGenerator.generate())
            addType(moduleGenerator.generate())
            addType(viewModelGenerator.generate())
            addType(componentGetterGenerator.generate())
            addType(destinationComponentGenerator.generate())
        }
    }
}
