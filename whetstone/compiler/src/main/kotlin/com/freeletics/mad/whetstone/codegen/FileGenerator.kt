package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.common.ComposeGenerator
import com.freeletics.mad.whetstone.codegen.common.NavDestinationModuleGenerator
import com.freeletics.mad.whetstone.codegen.common.RetainedComponentGenerator
import com.freeletics.mad.whetstone.codegen.common.ViewModelGenerator
import com.freeletics.mad.whetstone.codegen.compose.ComposeScreenGenerator
import com.freeletics.mad.whetstone.codegen.fragment.ComposeFragmentGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntryComponentGetterGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntrySubcomponentGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntryViewModelGenerator
import com.freeletics.mad.whetstone.codegen.fragment.RendererFragmentGenerator
import com.squareup.kotlinpoet.FileSpec

internal class FileGenerator{

    fun generate(data: ComposeScreenData): FileSpec {
        val retainedComponentGenerator = RetainedComponentGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val composeScreenGenerator = ComposeScreenGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addFunction(composeScreenGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationType(data)
            .addNavEntryTypes(data.navigation?.navEntryData)
            .build()
    }

    fun generate(data: ComposeFragmentData): FileSpec {
        val retainedComponentGenerator = RetainedComponentGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val composeFragmentGenerator = ComposeFragmentGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(composeFragmentGenerator.generate())
            .addFunction(composeGenerator.generate())
            .addNavDestinationType(data)
            .addNavEntryTypes(data.navigation?.navEntryData)
            .build()
    }

    fun generate(data: RendererFragmentData): FileSpec {
        val retainedComponentGenerator = RetainedComponentGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val rendererFragmentGenerator = RendererFragmentGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(rendererFragmentGenerator.generate())
            .addNavDestinationType(data)
            .addNavEntryTypes(data.navigation?.navEntryData)
            .build()
    }

    private fun FileSpec.Builder.addNavDestinationType(data: CommonData) = apply {
        if (data.navigation?.destinationMethod != null) {
            val navDestinationGenerator = NavDestinationModuleGenerator(data)
            addType(navDestinationGenerator.generate())
        }
    }

    private fun FileSpec.Builder.addNavEntryTypes(data: NavEntryData?) = apply {
        if (data != null) {
            val subcomponentGenerator = NavEntrySubcomponentGenerator(data)
            val viewModelGenerator = NavEntryViewModelGenerator(data)
            val componentGetterGenerator = NavEntryComponentGetterGenerator(data)

            addType(subcomponentGenerator.generate())
            addType(viewModelGenerator.generate())
            addType(componentGetterGenerator.generate())
        }
    }

    // for testing
    internal fun generate(data: NavEntryData): FileSpec {
        return FileSpec.builder(data.packageName, "WhetstoneNavEntry${data.baseName}")
            .addNavEntryTypes(data)
            .build()
    }
}
