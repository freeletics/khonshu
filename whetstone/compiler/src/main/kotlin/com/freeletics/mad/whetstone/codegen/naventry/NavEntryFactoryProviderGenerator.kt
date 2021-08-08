package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.bindsInstanceParameter
import com.freeletics.mad.whetstone.codegen.bundle
import com.freeletics.mad.whetstone.codegen.componentAnnotation
import com.freeletics.mad.whetstone.codegen.compositeDisposable
import com.freeletics.mad.whetstone.codegen.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.coroutineScope
import com.freeletics.mad.whetstone.codegen.internalApiAnnotation
import com.freeletics.mad.whetstone.codegen.internalWhetstoneApi
import com.freeletics.mad.whetstone.codegen.savedStateHandle
import com.freeletics.mad.whetstone.codegen.scopeToAnnotation
import com.freeletics.mad.whetstone.codegen.subcomponentAnnotation
import com.freeletics.mad.whetstone.codegen.subcomponentFactory
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec

internal val NavEntryGenerator.navEntrySubcomponentFactoryProviderClassName get() = ClassName("${navEntrySubcomponentClassName.simpleName}FactoryProvider")

internal val NavEntryGenerator.navEntrySubcomponentFactoryProviderGetterName get() = "get${navEntrySubcomponentClassName.simpleName}"

internal class NavEntryFactoryProviderGenerator(
    override val data: NavEntryData,
) : NavEntryGenerator() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(navEntrySubcomponentFactoryProviderClassName)
            .addAnnotation(contributesToAnnotation(data.parentScope))
            .addFunction(getter())
            .build()
    }

    private fun getter(): FunSpec {
        return FunSpec.builder(navEntrySubcomponentFactoryProviderGetterName)
            .addModifiers(ABSTRACT)
            .addAnnotation(internalWhetstoneApi)
            .returns(navEntrySubcomponentFactoryClassName)
            .build()
    }
}
