package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.bindsInstanceParameter
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.compositeDisposable
import com.freeletics.mad.whetstone.codegen.util.coroutineScope
import com.freeletics.mad.whetstone.codegen.util.internalApiAnnotation
import com.freeletics.mad.whetstone.codegen.util.savedStateHandle
import com.freeletics.mad.whetstone.codegen.util.scopeToAnnotation
import com.freeletics.mad.whetstone.codegen.util.subcomponentAnnotation
import com.freeletics.mad.whetstone.codegen.util.subcomponentFactory
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<NavEntryData>.navEntrySubcomponentClassName get() = ClassName("NavEntry${data.baseName}Component")

internal val Generator<NavEntryData>.navEntrySubcomponentFactoryClassName get() = navEntrySubcomponentClassName.nestedClass("Factory")

internal const val navEntrySubcomponentFactoryCreateName = "create"

internal class NavEntrySubcomponentGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(navEntrySubcomponentClassName)
            .addAnnotation(internalApiAnnotation())
            .addAnnotation(scopeToAnnotation(data.scope))
            .addAnnotation(subcomponentAnnotation(data.scope))
            .addType(navEntrySubcomponentFactory())
            .build()
    }

    private fun navEntrySubcomponentFactory(): TypeSpec {
        val createFun = FunSpec.builder(navEntrySubcomponentFactoryCreateName)
            .addModifiers(ABSTRACT)
            .addParameter(bindsInstanceParameter("savedStateHandle", savedStateHandle))
            .addParameter(bindsInstanceParameter("arguments", bundle))
            .apply {
                if (data.rxJavaEnabled) {
                    addParameter(bindsInstanceParameter("compositeDisposable", compositeDisposable))
                }
                if (data.coroutinesEnabled) {
                    addParameter(bindsInstanceParameter("coroutineScope", coroutineScope))
                }
            }
            .returns(navEntrySubcomponentClassName)
            .build()
        return TypeSpec.interfaceBuilder(navEntrySubcomponentFactoryClassName)
            .addAnnotation(subcomponentFactory)
            .addFunction(createFun)
            .build()
    }
}
