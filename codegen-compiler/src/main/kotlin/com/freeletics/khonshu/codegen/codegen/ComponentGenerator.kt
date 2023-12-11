package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.asParameter
import com.freeletics.khonshu.codegen.util.bindsInstanceParameter
import com.freeletics.khonshu.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.util.deepLinkHandler
import com.freeletics.khonshu.codegen.util.deepLinkPrefix
import com.freeletics.khonshu.codegen.util.forScope
import com.freeletics.khonshu.codegen.util.immutableSet
import com.freeletics.khonshu.codegen.util.navEventNavigator
import com.freeletics.khonshu.codegen.util.navigationDestination
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.scopeToAnnotation
import com.freeletics.khonshu.codegen.util.simplePropertySpec
import com.freeletics.khonshu.codegen.util.subcomponentAnnotation
import com.freeletics.khonshu.codegen.util.subcomponentFactoryAnnotation
import com.squareup.anvil.compiler.internal.decapitalize
import com.squareup.kotlinpoet.AnnotationSpec.UseSiteTarget.GET
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.Closeable

internal val Generator<out BaseData>.retainedComponentClassName
    get() = ClassName("Khonshu${data.baseName}Component")

internal const val retainedComponentFactoryCreateName = "create"

internal val Generator<out BaseData>.retainedComponentFactoryClassName
    get() = retainedComponentClassName.nestedClass("Factory")

internal const val closeableSetPropertyName = "closeables"

internal val Generator<out BaseData>.retainedParentComponentClassName
    get() = retainedComponentClassName.nestedClass("ParentComponent")

internal val Generator<out BaseData>.retainedParentComponentGetterName
    get() = "${retainedComponentClassName.simpleName.decapitalize()}Factory"

internal class ComponentGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(retainedComponentClassName)
            .addAnnotation(optInAnnotation())
            .addAnnotation(scopeToAnnotation(data.scope))
            .addAnnotation(subcomponentAnnotation(data.scope, data.parentScope))
            .addSuperinterface(Closeable::class)
            .addProperties(componentProperties())
            .addFunction(closeFunction())
            .addType(retainedComponentFactory())
            .addType(retainedComponentFactoryParentComponent())
            .build()
    }

    private fun componentProperties(): List<PropertySpec> {
        val properties = mutableListOf<PropertySpec>()
        properties += simplePropertySpec(data.stateMachine)

        properties += simplePropertySpec(navEventNavigator).toBuilder()
            .addAnnotation(forScope(data.scope, GET))
            .build()

        properties += data.composableParameter.map {
            PropertySpec.builder(it.name, it.typeName).build()
        }

        if (data is NavHostActivityData) {
            properties += listOf(
                PropertySpec.builder("destinations", immutableSet.parameterizedBy(navigationDestination)).build(),
                PropertySpec.builder("deepLinkHandlers", immutableSet.parameterizedBy(deepLinkHandler)).build(),
                PropertySpec.builder("deepLinkPrefixes", immutableSet.parameterizedBy(deepLinkPrefix)).build(),
            )
        }
        properties += PropertySpec.builder(closeableSetPropertyName, SET.parameterizedBy(Closeable::class.asTypeName()))
            .addAnnotation(forScope(data.scope, GET))
            .build()
        return properties
    }

    private fun closeFunction(): FunSpec {
        return FunSpec.builder("close")
            .addModifiers(OVERRIDE)
            .beginControlFlow("%L.forEach {", closeableSetPropertyName)
            .addStatement("it.close()")
            .endControlFlow()
            .build()
    }

    private fun retainedComponentFactory(): TypeSpec {
        val createFun = FunSpec.builder(retainedComponentFactoryCreateName)
            .addModifiers(ABSTRACT)
            .addParameter(bindsInstanceParameter("savedStateHandle", savedStateHandle, forScope(data.scope)))
            .addParameter(
                bindsInstanceParameter(
                    data.navigation.asParameter(),
                    forScope(data.scope).takeIf { data.navigation == null },
                ),
            )
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
