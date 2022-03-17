package com.freeletics.mad.whetstone.codegen

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
        val navDestinationGenerator = NavDestinationModuleGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addFunction(composeScreenGenerator.generate())
            .addFunction(composeGenerator.generate())
            .also {
                if (data.navigation?.destinationMethod != null) {
                    it.addType(navDestinationGenerator.generate())
                }
            }
            .build()
    }

    fun generate(data: ComposeFragmentData): FileSpec {
        val retainedComponentGenerator = RetainedComponentGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val composeFragmentGenerator = ComposeFragmentGenerator(data)
        val composeGenerator = ComposeGenerator(data)
        val navDestinationGenerator = NavDestinationModuleGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(composeFragmentGenerator.generate())
            .addFunction(composeGenerator.generate())
            .also {
                if (data.navigation?.destinationMethod != null) {
                    it.addType(navDestinationGenerator.generate())
                }
            }
            .build()
    }

    fun generate(data: RendererFragmentData): FileSpec {
        val retainedComponentGenerator = RetainedComponentGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val rendererFragmentGenerator = RendererFragmentGenerator(data)
        val navDestinationGenerator = NavDestinationModuleGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(rendererFragmentGenerator.generate())
            .also {
                if (data.navigation?.destinationMethod != null) {
                    it.addType(navDestinationGenerator.generate())
                }
            }
            .build()
    }

    fun generate(data: NavEntryData): FileSpec {
        val subcomponentGenerator = NavEntrySubcomponentGenerator(data)
        val viewModelGenerator = NavEntryViewModelGenerator(data)
        val componentGetterGenerator = NavEntryComponentGetterGenerator(data)

        return FileSpec.builder(data.packageName, "WhetstoneNavEntry${data.baseName}")
            .addType(subcomponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(componentGetterGenerator.generate())
            .build()
    }
}
