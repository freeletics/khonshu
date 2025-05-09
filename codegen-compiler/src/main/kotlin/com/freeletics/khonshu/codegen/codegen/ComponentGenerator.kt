package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.activityNavigator
import com.freeletics.khonshu.codegen.util.asParameter
import com.freeletics.khonshu.codegen.util.bindsInstanceParameter
import com.freeletics.khonshu.codegen.util.contributesToAnnotation
import com.freeletics.khonshu.codegen.util.forScope
import com.freeletics.khonshu.codegen.util.hostNavigator
import com.freeletics.khonshu.codegen.util.multiStackHostNavigatorViewModel
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.scopeToAnnotation
import com.freeletics.khonshu.codegen.util.simplePropertySpec
import com.freeletics.khonshu.codegen.util.subcomponentAnnotation
import com.freeletics.khonshu.codegen.util.subcomponentFactoryAnnotation
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

internal const val RETAINED_COMPONENT_FACTORY_CREATE_NAME = "create"

internal val Generator<out BaseData>.retainedComponentFactoryClassName
    get() = retainedComponentClassName.nestedClass("Factory")

internal const val CLOSEABLE_SET_PROPERTY_NAME = "closeables"

internal val Generator<out BaseData>.retainedParentComponentClassName
    get() = retainedComponentClassName.nestedClass("ParentComponent")

internal val Generator<out BaseData>.retainedParentComponentGetterName
    get() = "${retainedComponentClassName.simpleName.replaceFirstChar { it.lowercase() }}Factory"

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

        if (data is NavHostActivityData) {
            properties += simplePropertySpec(hostNavigator)
        } else {
            properties += simplePropertySpec(activityNavigator).toBuilder()
                .addAnnotation(forScope(data.scope, GET))
                .build()
        }

        properties += data.composableParameter.map {
            PropertySpec.builder(it.name, it.typeName).build()
        }

        properties += PropertySpec.builder(
            CLOSEABLE_SET_PROPERTY_NAME,
            SET.parameterizedBy(Closeable::class.asTypeName()),
        )
            .addAnnotation(forScope(data.scope, GET))
            .build()

        return properties
    }

    private fun closeFunction(): FunSpec {
        return FunSpec.builder("close")
            .addModifiers(OVERRIDE)
            .beginControlFlow("%L.forEach {", CLOSEABLE_SET_PROPERTY_NAME)
            .addStatement("it.close()")
            .endControlFlow()
            .build()
    }

    private fun retainedComponentFactory(): TypeSpec {
        val createFun = FunSpec.builder(RETAINED_COMPONENT_FACTORY_CREATE_NAME)
            .addModifiers(ABSTRACT)
            .apply {
                if (data is NavHostActivityData) {
                    addParameter(bindsInstanceParameter("viewModel", multiStackHostNavigatorViewModel))
                }
            }
            .addParameter(bindsInstanceParameter("savedStateHandle", savedStateHandle, forScope(data.scope)))
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
