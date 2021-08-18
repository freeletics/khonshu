package com.freeletics.mad.whetstone.codegen.renderer

import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.fragment
import com.freeletics.mad.whetstone.codegen.util.lateinitPropertySpec
import com.freeletics.mad.whetstone.codegen.util.layoutInflater
import com.freeletics.mad.whetstone.codegen.util.navigationHandlerHandle
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.rendererConnect
import com.freeletics.mad.whetstone.codegen.util.view
import com.freeletics.mad.whetstone.codegen.util.viewGroup
import com.freeletics.mad.whetstone.codegen.util.viewModelProvider
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.TypeSpec

internal class RendererFragmentGenerator(
    override val data: RendererFragmentData,
) : Generator<RendererFragmentData>() {

    private val rendererFragmentClassName = ClassName("${data.baseName}Fragment")

    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(rendererFragmentClassName)
            .addAnnotation(optInAnnotation())
            .superclass(data.fragmentBaseClass)
            .addProperty(lateinitPropertySpec(data.factory))
            .addProperty(lateinitPropertySpec(data.stateMachine))
            .addFunction(rendererOnCreateViewFun())
            .addFunction(rendererInjectFun())
            .build()
    }

    private fun rendererOnCreateViewFun(): FunSpec {
        return FunSpec.builder("onCreateView")
            .addModifiers(OVERRIDE)
            .addParameter("inflater", layoutInflater)
            .addParameter("container", viewGroup.copy(nullable = true))
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .returns(view)
            .beginControlFlow("if (!::%L.isInitialized)", data.stateMachine.propertyName)
            .addStatement("%L()", rendererFragmentInjectName)
            .endControlFlow()
            .addCode("\n")
            // inflate: external method
            .addStatement("val renderer = %L.inflate(inflater, container)", data.factory.propertyName)
            // connect: external method
            .addStatement("%M(renderer, %L)", rendererConnect, data.stateMachine.propertyName)
            .addStatement("return renderer.rootView")
            .build()
    }

    private val rendererFragmentInjectName = "inject"

    private fun rendererInjectFun(): FunSpec {
        return FunSpec.builder(rendererFragmentInjectName)
            .addModifiers(PRIVATE)
            .beginControlFlow("val viewModelProvider = %M<%T>(this, %T::class) { dependencies, handle -> ", viewModelProvider, data.dependencies, data.parentScope)
            // arguments: external method
            .addStatement("val arguments = arguments ?: %T.EMPTY", bundle)
            .addStatement("%T(dependencies, handle, arguments)", viewModelClassName)
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
        if (data.navigation == null) {
            return CodeBlock.of("")
        }

        return CodeBlock.builder()
            .add("\n")
            .addStatement("val handler = component.%L", data.navigation.navigationHandler.propertyName)
            .addStatement("val navigator = component.%L", data.navigation.navigator.propertyName)
            // lifecycle: external method
            .addStatement("handler.%N(this, navigator)", navigationHandlerHandle)
            .build()
    }
}
