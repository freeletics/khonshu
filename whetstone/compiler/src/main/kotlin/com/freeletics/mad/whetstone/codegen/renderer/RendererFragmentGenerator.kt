package com.freeletics.mad.whetstone.codegen.renderer

import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.fragmentConverter
import com.freeletics.mad.whetstone.codegen.util.fragmentNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.fragmentViewModelProvider
import com.freeletics.mad.whetstone.codegen.util.lateinitPropertySpec
import com.freeletics.mad.whetstone.codegen.util.layoutInflater
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.rendererConnect
import com.freeletics.mad.whetstone.codegen.util.view
import com.freeletics.mad.whetstone.codegen.util.viewGroup
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec

internal class RendererFragmentGenerator(
    override val data: RendererFragmentData,
) : Generator<RendererFragmentData>() {

    private val rendererFragmentClassName = ClassName("${data.baseName}Fragment")

    internal fun generate(): TypeSpec {
        val argumentsParameter = data.navigation.asParameter()
        return TypeSpec.classBuilder(rendererFragmentClassName)
            .addAnnotation(optInAnnotation())
            .superclass(data.fragmentBaseClass)
            .addProperty(lateinitPropertySpec(data.factory))
            .addProperty(lateinitPropertySpec(data.stateMachine))
            .addFunction(rendererOnCreateViewFun(argumentsParameter))
            .addFunction(rendererInjectFun(argumentsParameter))
            .build()
    }

    private fun rendererOnCreateViewFun(argumentsParameter: ParameterSpec): FunSpec {
        return FunSpec.builder("onCreateView")
            .addModifiers(OVERRIDE)
            .addParameter("inflater", layoutInflater)
            .addParameter("container", viewGroup.copy(nullable = true))
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .returns(view)
            .beginControlFlow("if (!::%L.isInitialized)", data.stateMachine.propertyName)
            .addCode("val %N = ", argumentsParameter)
            .addCode(data.navigation.fragmentConverter())
            .addCode("\n")
            .addStatement("%L(%N)", rendererFragmentInjectName, argumentsParameter)
            .endControlFlow()
            .addCode("\n")
            // inflate: external method
            .addStatement("val renderer = %L.inflate(inflater, container)", data.factory.propertyName)
            .addStatement("%M(renderer, %L)", rendererConnect, data.stateMachine.propertyName)
            .addStatement("return renderer.rootView")
            .build()
    }

    private val rendererFragmentInjectName = "inject"

    private fun rendererInjectFun(argumentsParameter: ParameterSpec): FunSpec {
        return FunSpec.builder(rendererFragmentInjectName)
            .addModifiers(PRIVATE)
            .addParameter(argumentsParameter)
            .beginControlFlow("val viewModelProvider = %M<%T>(this, %T::class) { dependencies, handle -> ", fragmentViewModelProvider, data.dependencies, data.parentScope)
            .addStatement("%T(dependencies, handle, %N)", viewModelClassName, argumentsParameter)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("val component = viewModel.%L", viewModelComponentName)
            .addCode("\n")
            .addStatement("%1L = component.%1L", data.factory.propertyName)
            .addStatement("%1L = component.%1L", data.stateMachine.propertyName)
            .addCode(rendererNavigationCode())
            .build()
    }

    private fun rendererNavigationCode(): CodeBlock {
        val navigator = data.navigation?.navigator
        if (navigator == null) {
            return CodeBlock.of("")
        }

        return CodeBlock.builder()
            .add("\n")
            .addStatement("val navigator = component.%L", navigator.propertyName)
            .addStatement("%M(this, navigator)", fragmentNavigationHandler)
            .build()
    }
}
