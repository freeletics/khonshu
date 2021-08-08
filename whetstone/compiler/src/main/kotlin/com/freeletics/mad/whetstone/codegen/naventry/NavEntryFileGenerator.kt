package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.squareup.kotlinpoet.FileSpec

internal class NavEntryFileGenerator(
    private val data: NavEntryData,
) {

    private val subcomponentGenerator = NavEntrySubcomponentGenerator(data)
    private val factoryProviderGenerator = NavEntryFactoryProviderGenerator(data)
    private val viewModelGenerator = NavEntryViewModelGenerator(data)
    private val componentGetterGenerator = NavEntryComponentGetterGenerator(data)
    private val componentGetterGenerator2 = NavEntryComponentGetterModuleGenerator(data)

    fun generate(): FileSpec {
        return FileSpec.builder(data.packageName, "WhetstoneNavEntry${data.baseName}")
            .addType(subcomponentGenerator.generate())
            .addType(factoryProviderGenerator.generate())
            .addType(viewModelGenerator.generate())
            .addType(componentGetterGenerator.generate())
            .addType(componentGetterGenerator2.generate())
            .build()
    }
}
