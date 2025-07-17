package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.util.activityNavigator
import com.freeletics.khonshu.codegen.util.asParameter
import com.freeletics.khonshu.codegen.util.autoCloseable
import com.freeletics.khonshu.codegen.util.contributesGraphExtension
import com.freeletics.khonshu.codegen.util.contributesGraphExtensionFactory
import com.freeletics.khonshu.codegen.util.forScope
import com.freeletics.khonshu.codegen.util.hostNavigator
import com.freeletics.khonshu.codegen.util.multiStackHostNavigatorViewModel
import com.freeletics.khonshu.codegen.util.multibinds
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.providesParameter
import com.freeletics.khonshu.codegen.util.savedStateHandle
import com.freeletics.khonshu.codegen.util.simplePropertySpec
import com.freeletics.khonshu.codegen.util.singleIn
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.graphClassName
    get() = ClassName("Khonshu${data.baseName}Graph")

internal val Generator<out BaseData>.graphFactoryClassName
    get() = graphClassName.nestedClass("Factory")

internal val Generator<out BaseData>.graphFactoryCreateFunctionName
    get() = "create${graphClassName.simpleName}"

internal const val CLOSEABLE_SET_PROPERTY_NAME = "closeables"

internal class GraphGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {
    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(graphClassName)
            .addAnnotation(optIn())
            .addAnnotation(singleIn(data.scope))
            .addAnnotation(contributesGraphExtension(data.scope))
            .addSuperinterface(autoCloseable)
            .addProperties(graphProperties())
            .addFunction(multibindsCloseableFunction())
            .addFunction(closeFunction())
            .addType(retainedGraphFactory())
            .build()
    }

    private fun graphProperties(): List<PropertySpec> {
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
            SET.parameterizedBy(autoCloseable),
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
            .returns(SET.parameterizedBy(autoCloseable))
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

    private fun retainedGraphFactory(): TypeSpec {
        val createFun = FunSpec.builder(graphFactoryCreateFunctionName)
            .addModifiers(ABSTRACT)
            .apply {
                if (data is NavHostActivityData) {
                    addParameter(providesParameter("viewModel", multiStackHostNavigatorViewModel))
                }
            }
            .addParameter(providesParameter("savedStateHandle", savedStateHandle, forScope(data.scope)))
            .addParameter(providesParameter(data.navigation.asParameter()))
            .returns(graphClassName)
            .build()
        return TypeSpec.interfaceBuilder(graphFactoryClassName)
            .addAnnotation(contributesGraphExtensionFactory(data.parentScope))
            .addFunction(createFun)
            .build()
    }
}
