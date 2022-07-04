package com.freeletics.mad.whetstone.codegen.common

import com.freeletics.mad.whetstone.BaseData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.internalApiAnnotation
import com.freeletics.mad.whetstone.codegen.util.savedStateHandle
import com.freeletics.mad.whetstone.codegen.util.viewModel
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out BaseData>.viewModelClassName
    get() = ClassName("Whetstone${data.baseName}ViewModel")

internal const val viewModelComponentName = "component"

internal class ViewModelGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {

    internal fun generate(): TypeSpec {
        val argumentsParameter = data.navigation.asParameter()
        return TypeSpec.classBuilder(viewModelClassName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(internalApiAnnotation())
            .superclass(viewModel)
            .primaryConstructor(viewModelCtor(argumentsParameter))
            .addProperty(viewModelProperty(argumentsParameter))
            .addFunction(viewModelOnClearedFun())
            .build()
    }

    private fun viewModelCtor(argumentsParameter: ParameterSpec): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("parentComponent", retainedParentComponentClassName)
            .addParameter("savedStateHandle", savedStateHandle)
            .addParameter(argumentsParameter)
            .build()
    }

    private fun viewModelProperty(argumentsParameter: ParameterSpec): PropertySpec {
        val componentInitializer = CodeBlock.builder().add(
            "parentComponent.%L().%L(savedStateHandle, %N)",
            retainedParentComponentGetterName,
            retainedComponentFactoryCreateName,
            argumentsParameter,
        ).build()

        return PropertySpec.builder(viewModelComponentName, retainedComponentClassName)
            .initializer(componentInitializer)
            .build()
    }

    private fun viewModelOnClearedFun(): FunSpec {
        return FunSpec.builder("onCleared")
            .addModifiers(PUBLIC, OVERRIDE)
            .beginControlFlow("%L.%L.forEach", viewModelComponentName, closeableSetPropertyName)
            .addStatement("it.close()")
            .endControlFlow()
            .build()
    }
}
