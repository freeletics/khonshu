package com.freeletics.mad.whetstone.codegen.common

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.compositeDisposable
import com.freeletics.mad.whetstone.codegen.util.coroutineScope
import com.freeletics.mad.whetstone.codegen.util.coroutineScopeCancel
import com.freeletics.mad.whetstone.codegen.util.internalApiAnnotation
import com.freeletics.mad.whetstone.codegen.util.mainScope
import com.freeletics.mad.whetstone.codegen.util.savedStateHandle
import com.freeletics.mad.whetstone.codegen.util.viewModel
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<out CommonData>.viewModelClassName
    get() = ClassName("${data.baseName}ViewModel")

internal const val viewModelComponentName = "component"

internal class ViewModelGenerator(
    override val data: CommonData,
) : Generator<CommonData>() {

    internal fun generate(): TypeSpec {
        val argumentsParameter = data.navigation.asParameter()
        return TypeSpec.classBuilder(viewModelClassName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(internalApiAnnotation())
            .superclass(viewModel)
            .primaryConstructor(viewModelCtor(argumentsParameter))
            .addProperties(viewModelProperties(argumentsParameter))
            .addFunction(viewModelOnClearedFun())
            .build()
    }

    private fun viewModelCtor(argumentsParameter: ParameterSpec): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("dependencies", data.dependencies)
            .addParameter("savedStateHandle", savedStateHandle)
            .addParameter(argumentsParameter)
            .build()
    }

    private fun viewModelProperties(argumentsParameter: ParameterSpec): List<PropertySpec> {
        val properties = mutableListOf<PropertySpec>()
        val componentInitializer = CodeBlock.builder().add(
            "%T.factory().%L(dependencies, savedStateHandle, %N",
            retainedComponentClassName.peerClass("Dagger${retainedComponentClassName.simpleName}"),
            retainedComponentFactoryCreateName,
            argumentsParameter,
        )
        if (data.rxJavaEnabled) {
            properties += PropertySpec.builder("disposable", compositeDisposable)
                .addModifiers(PRIVATE)
                .initializer("%T()", compositeDisposable)
                .build()
            componentInitializer.add(", disposable")
        }
        if (data.coroutinesEnabled) {
            properties += PropertySpec.builder("scope", coroutineScope)
                .addModifiers(PRIVATE)
                .initializer("%M()", mainScope)
                .build()
            componentInitializer.add(", scope")
        }
        componentInitializer.add(")")
        properties += PropertySpec.builder(viewModelComponentName, retainedComponentClassName)
            .initializer(componentInitializer.build())
            .build()
        return properties
    }

    private fun viewModelOnClearedFun(): FunSpec {
        val codeBuilder = CodeBlock.builder()
        if (data.rxJavaEnabled) {
            codeBuilder.addStatement("disposable.clear()")
        }
        if (data.coroutinesEnabled) {
            codeBuilder.addStatement("scope.%M()", coroutineScopeCancel)
        }
        return FunSpec.builder("onCleared")
            .addModifiers(PUBLIC, OVERRIDE)
            .addCode(codeBuilder.build())
            .build()
    }
}
