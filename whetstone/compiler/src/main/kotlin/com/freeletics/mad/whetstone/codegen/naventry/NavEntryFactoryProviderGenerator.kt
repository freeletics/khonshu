package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.internalWhetstoneApi
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<NavEntryData>.navEntrySubcomponentFactoryProviderClassName get() = ClassName("${navEntrySubcomponentClassName.simpleName}FactoryProvider")

internal val Generator<NavEntryData>.navEntrySubcomponentFactoryProviderGetterName get() = "get${navEntrySubcomponentClassName.simpleName}"

internal class NavEntryFactoryProviderGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {

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
