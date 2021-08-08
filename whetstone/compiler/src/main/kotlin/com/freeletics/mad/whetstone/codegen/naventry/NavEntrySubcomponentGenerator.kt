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

internal val NavEntryGenerator.navEntrySubcomponentClassName get() = ClassName("NavEntry${data.baseName}Component")

internal val NavEntryGenerator.navEntrySubcomponentFactoryClassName get() = navEntrySubcomponentClassName.nestedClass("Factory")

internal const val navEntrySubcomponentFactoryCreateName = "create"

internal class NavEntrySubcomponentGenerator(
    override val data: NavEntryData,
) : NavEntryGenerator() {

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
