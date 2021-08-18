package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.common.RetainedComponentGenerator
import com.freeletics.mad.whetstone.codegen.common.ViewModelGenerator
import com.freeletics.mad.whetstone.codegen.compose.ComposeFragmentGenerator
import com.freeletics.mad.whetstone.codegen.compose.ComposeGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntryComponentGetterGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntryComponentGetterModuleGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntryFactoryProviderGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntrySubcomponentGenerator
import com.freeletics.mad.whetstone.codegen.naventry.NavEntryViewModelGenerator
import com.freeletics.mad.whetstone.codegen.renderer.RendererFragmentGenerator
import com.freeletics.mad.whetstone.codegen.util.simplePropertySpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec

internal class FileGenerator{

    fun generate(data: ComposeScreenData): FileSpec {
        val retainedComponentGenerator = RetainedComponentGenerator(data)
        val viewModelGenerator = ViewModelGenerator(data)
        val composeGenerator = ComposeGenerator(data)

        return FileSpec.builder(data.packageName, "Whetstone${data.baseName}")
            .addType(retainedComponentGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addFunction(composeGenerator.generate(disableNavigation = false))
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
            .addFunction(composeGenerator.generate(disableNavigation = true))
            .addType(composeFragmentGenerator.generate())
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
            .build()
    }

    fun generate(data: NavEntryData): FileSpec {
        val subcomponentGenerator = NavEntrySubcomponentGenerator(data)
        val factoryProviderGenerator = NavEntryFactoryProviderGenerator(data)
        val viewModelGenerator = NavEntryViewModelGenerator(data)
        val componentGetterGenerator = NavEntryComponentGetterGenerator(data)
        val componentGetterModuleGenerator = NavEntryComponentGetterModuleGenerator(data)

        return FileSpec.builder(data.packageName, "WhetstoneNavEntry${data.baseName}")
            .addType(subcomponentGenerator.generate())
            .addType(factoryProviderGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(componentGetterGenerator.generate())
            .addType(componentGetterModuleGenerator.generate())
            .build()
    }
}
