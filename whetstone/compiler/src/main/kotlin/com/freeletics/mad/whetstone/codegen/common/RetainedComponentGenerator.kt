package com.freeletics.mad.whetstone.codegen.common

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.bindsInstanceParameter
import com.freeletics.mad.whetstone.codegen.util.composeProviderValueModule
import com.freeletics.mad.whetstone.codegen.util.contributesToAnnotation
import com.freeletics.mad.whetstone.codegen.util.navEventNavigator
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.providedValue
import com.freeletics.mad.whetstone.codegen.util.savedStateHandle
import com.freeletics.mad.whetstone.codegen.util.scopeToAnnotation
import com.freeletics.mad.whetstone.codegen.util.simplePropertySpec
import com.freeletics.mad.whetstone.codegen.util.subcomponentAnnotation
import com.freeletics.mad.whetstone.codegen.util.subcomponentFactoryAnnotation
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.decapitalize
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.Closeable

internal val Generator<out CommonData>.retainedComponentClassName
    get() = ClassName("Retained${data.baseName}Component")

internal const val retainedComponentFactoryCreateName = "create"

internal val Generator<out CommonData>.retainedComponentFactoryClassName
    get() = retainedComponentClassName.nestedClass("Factory")

internal const val providedValueSetPropertyName = "providedValues"
internal const val closeableSetPropertyName = "closeables"

internal val Generator<out CommonData>.retainedParentComponentClassName
    get() = retainedComponentClassName.nestedClass("ParentComponent")

@OptIn(ExperimentalAnvilApi::class)
internal val Generator<out CommonData>.retainedParentComponentGetterName
    get() = "${retainedComponentClassName.simpleName.decapitalize()}Factory"

internal class RetainedComponentGenerator(
    override val data: CommonData,
) : Generator<CommonData>() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(retainedComponentClassName)
            .addAnnotation(optInAnnotation())
            .addAnnotation(scopeToAnnotation(data.scope))
            .addAnnotation(subcomponentAnnotation(data.scope, data.parentScope, moduleClassName()))
            .addProperties(componentProperties())
            .addType(retainedComponentFactory())
            .addType(retainedComponentFactoryParentComponent())
            .build()
    }

    private fun moduleClassName(): ClassName? {
        return when (data) {
            is ComposeFragmentData -> composeProviderValueModule
            is ComposeScreenData -> composeProviderValueModule
            is RendererFragmentData -> null
        }
    }

    private fun componentProperties(): List<PropertySpec> {
        val properties = mutableListOf<PropertySpec>()
        properties += simplePropertySpec(data.stateMachine)
        if (data.navigation != null) {
            properties += simplePropertySpec(navEventNavigator)
        }
        properties += PropertySpec.builder(closeableSetPropertyName, SET.parameterizedBy(Closeable::class.asTypeName())).build()
        properties += when (data) {
            is ComposeFragmentData -> providedValueSetProperty()
            is ComposeScreenData -> providedValueSetProperty()
            is RendererFragmentData -> simplePropertySpec(data.factory)
        }
        return properties
    }

    private fun providedValueSetProperty(): PropertySpec {
        val type = SET.parameterizedBy(providedValue.parameterizedBy(STAR))
        return PropertySpec.builder(providedValueSetPropertyName, type).build()
    }

    private fun retainedComponentFactory(): TypeSpec {
        val createFun = FunSpec.builder(retainedComponentFactoryCreateName)
            .addModifiers(ABSTRACT)
            .addParameter(bindsInstanceParameter("savedStateHandle", savedStateHandle))
            .addParameter(bindsInstanceParameter(data.navigation.asParameter()))
            .returns(retainedComponentClassName)
            .build()
        return TypeSpec.interfaceBuilder(retainedComponentFactoryClassName)
            .addAnnotation(subcomponentFactoryAnnotation())
            .addFunction(createFun)
            .build()
    }

    private fun retainedComponentFactoryParentComponent(): TypeSpec {
        val getterFun = FunSpec.builder(retainedParentComponentGetterName)
            .addModifiers(ABSTRACT)
            .returns(retainedComponentFactoryClassName)
            .build()
        return TypeSpec.interfaceBuilder(retainedParentComponentClassName)
            .addAnnotation(contributesToAnnotation(data.parentScope))
            .addFunction(getterFun)
            .build()
    }
}
