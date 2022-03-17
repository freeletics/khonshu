package com.freeletics.mad.whetstone.codegen.naventry

import com.freeletics.mad.whetstone.NavEntryData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.bundle
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
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal val Generator<NavEntryData>.viewModelClassName
    get() = ClassName("${data.baseName}ViewModel")

internal const val viewModelComponentName = "component"

internal class NavEntryViewModelGenerator(
    override val data: NavEntryData,
) : Generator<NavEntryData>() {

    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(viewModelClassName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(internalApiAnnotation())
            .superclass(viewModel)
            .primaryConstructor(viewModelCtor())
            .addProperties(viewModelProperties())
            .addFunction(viewModelOnClearedFun())
            .build()
    }

    private fun viewModelCtor(): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameter("factory", navEntrySubcomponentFactoryClassName)
            .addParameter("savedStateHandle", savedStateHandle)
            .addParameter("arguments", bundle)
            .build()
    }

    private fun viewModelProperties(): List<PropertySpec> {
        val properties = mutableListOf<PropertySpec>()
        val componentInitializer = CodeBlock.builder().add(
            "factory.%L(savedStateHandle, arguments",
            navEntrySubcomponentFactoryCreateName,
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
        properties += PropertySpec.builder(viewModelComponentName, navEntrySubcomponentClassName)
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
