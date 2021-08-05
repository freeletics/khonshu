package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Extra
import com.freeletics.mad.whetstone.Data
import com.squareup.anvil.annotations.MergeComponent
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal val Generator.retainedComponentClassName get() = ClassName("Retained${data.baseName}Component")

internal const val retainedComponentFactoryCreateName = "create"

internal class RetainedComponentGenerator(
    override val data: Data,
) : Generator() {

    fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(retainedComponentClassName)
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(internalApiAnnotation())
            .addAnnotation(retainedScopeAnnotation())
            .addAnnotation(componentAnnotation())
            .addProperties(componentProperties())
            .addType(retainedComponentFactory())
            .build()
    }

    private fun retainedScopeAnnotation(): AnnotationSpec {
        return AnnotationSpec.builder(scopeTo)
            .addMember("%T::class", data.scope)
            .build()
    }

    private fun componentAnnotation(): AnnotationSpec {
        return AnnotationSpec.builder(MergeComponent::class)
            .addMember("scope = %T::class", data.scope)
            .addMember("dependencies = [%T::class]", data.dependencies)
            .build()
    }

    private fun componentProperties(): List<PropertySpec> {
        val properties = mutableListOf<PropertySpec>()
        properties += simplePropertySpec(data.stateMachine)
        if (data.navigation != null) {
            properties += simplePropertySpec(data.navigation.navigator)
            properties += simplePropertySpec(data.navigation.navigationHandler)
        }
        if (data.extra is Extra.Renderer) {
            properties += simplePropertySpec(data.extra.factory)
        }
        return properties
    }

    private val retainedComponentFactoryClassName = retainedComponentClassName.peerClass("Factory")

    private fun retainedComponentFactory(): TypeSpec {
        val createFun = FunSpec.builder(retainedComponentFactoryCreateName)
            .addModifiers(ABSTRACT)
            .addParameter("dependencies", data.dependencies)
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
            .returns(retainedComponentClassName)
            .build()
        return TypeSpec.interfaceBuilder(retainedComponentFactoryClassName)
            .addAnnotation(componentFactory)
            .addFunction(createFun)
            .build()
    }
}
