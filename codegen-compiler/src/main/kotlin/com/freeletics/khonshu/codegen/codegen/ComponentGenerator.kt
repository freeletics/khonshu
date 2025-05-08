package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.activityNavigator
import com.freeletics.khonshu.codegen.util.asParameter
import com.freeletics.khonshu.codegen.util.forScope
import com.freeletics.khonshu.codegen.util.hostNavigator
import com.freeletics.khonshu.codegen.util.multiStackHostNavigatorViewModel
import com.freeletics.khonshu.codegen.util.multibinds
import com.freeletics.khonshu.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.util.providesParameter
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.simplePropertySpec
import com.freeletics.khonshu.codegen.util.singleInAnnotation
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
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import java.io.Closeable

internal val Generator<out BaseData>.retainedComponentClassName
    get() = ClassName("Khonshu${data.baseName}Component")

internal val Generator<out BaseData>.retainedComponentFactoryClassName
    get() = retainedComponentClassName.nestedClass("Factory")

internal val Generator<out BaseData>.retainedComponentFactoryCreateName
    get() = "create${retainedComponentClassName.simpleName}"

internal const val CLOSEABLE_SET_PROPERTY_NAME = "closeables"

internal class ComponentGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {
    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(retainedComponentClassName)
            .addAnnotation(optInAnnotation())
            .addAnnotation(singleInAnnotation(data.scope))
            .addAnnotation(subcomponentAnnotation(data.scope))
            .addSuperinterface(Closeable::class)
            .addProperties(componentProperties())
            .addFunction(multibindsCloseableFunction())
            .addFunction(closeFunction())
            .addType(retainedComponentFactory())
            .build()
    }

    private fun componentProperties(): List<PropertySpec> {
        val properties = mutableListOf<PropertySpec>()
        properties += simplePropertySpec(data.stateMachine)

        if (data is NavHostActivityData) {
            properties += simplePropertySpec(hostNavigator)
        } else {
            properties += simplePropertySpec(activityNavigator).toBuilder()
                .addAnnotation(forScope(data.scope))
                .build()
        }

        properties += data.composableParameter.map {
            PropertySpec.builder(it.name, it.typeName).build()
        }

        properties += PropertySpec.builder(
            CLOSEABLE_SET_PROPERTY_NAME,
            SET.parameterizedBy(Closeable::class.asTypeName()),
        )
            .addAnnotation(forScope(data.scope))
            .build()

        return properties
    }

    private fun multibindsCloseableFunction(): FunSpec {
        return FunSpec.builder("bindCloseables")
            .addModifiers(ABSTRACT)
            .addAnnotation(multibinds(allowEmpty = true))
            .addAnnotation(forScope(data.scope))
            .returns(SET.parameterizedBy(Closeable::class.asClassName()))
            .build()
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
        val createFun = FunSpec.builder(retainedComponentFactoryCreateName)
            .addModifiers(ABSTRACT)
            .apply {
                if (data is NavHostActivityData) {
                    addParameter(providesParameter("viewModel", multiStackHostNavigatorViewModel))
                }
            }
            .addParameter(providesParameter("savedStateHandle", savedStateHandle, forScope(data.scope)))
            .addParameter(providesParameter(data.navigation.asParameter()))
            .returns(retainedComponentClassName)
            .build()
        return TypeSpec.interfaceBuilder(retainedComponentFactoryClassName)
            .addAnnotation(subcomponentFactoryAnnotation(data.parentScope))
            .addFunction(createFun)
            .build()
    }
}
