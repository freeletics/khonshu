package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Data
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal class ComposeFragmentGenerator(
    override val data: Data,
) : Generator() {

    private val composeFragmentClassName = ClassName("${data.baseName}Fragment")

    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(composeFragmentClassName)
            .addAnnotation(optInAnnotation())
            .superclass(fragment)
            .addFunction(composeOnCreateViewFun())
            .apply {
                if (data.navigation != null) {
                    addProperty(navigationSetupProperty())
                    addFunction(setupNavigationFun())
                }
            }
            .build()
    }


    private fun composeOnCreateViewFun(): FunSpec {
        return FunSpec.builder("onCreateView")
            .addModifiers(OVERRIDE)
            .addParameter("inflater", layoutInflater)
            .addParameter("container", viewGroup.copy(nullable = true))
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .returns(view)
            .apply {
                if (data.navigation != null) {
                    beginControlFlow("if (!%L)", navigationSetupPropertyName)
                    addStatement("%L = true", navigationSetupPropertyName)
                    addStatement("%L()", navigationSetupFunctionName)
                    endControlFlow()
                    addCode("\n")
                }
            }
            .addStatement("val navController = %M()", findNavController)
            // requireContext: external method
            .addStatement("val composeView = %T(requireContext())", composeView)
            // setContent: external method
            .beginControlFlow("composeView.setContent {")
            .addStatement("%L(navController)", composableName)
            .endControlFlow()
            .addStatement("return composeView")
            .build()
    }

    private val navigationSetupPropertyName = "navigationSetup"

    private fun navigationSetupProperty(): PropertySpec {
        return PropertySpec.builder(navigationSetupPropertyName, BOOLEAN, PRIVATE)
            .mutable(true)
            .initializer("false")
            .build()
    }

    private val navigationSetupFunctionName = "setupNavigation"

    private fun setupNavigationFun(): FunSpec {
        return FunSpec.builder("setupNavigation")
            .addModifiers(PRIVATE)
            .beginControlFlow("val viewModelProvider = %M<%T>(this, %T::class) { dependencies, handle -> ", viewModelProvider, data.dependencies, data.parentScope)
            // arguments: external method
            .addStatement("val arguments = arguments ?: %T.EMPTY", bundle)
            .addStatement("%T(dependencies, handle, arguments)", viewModelClassName)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("val component = viewModel.%L", viewModelComponentName)
            .addCode("\n")
            .addStatement("val handler = component.%L", data.navigation!!.navigationHandler.propertyName)
            .addStatement("val navigator = component.%L", data.navigation.navigator.propertyName)
            // lifecycle: external method
            .addStatement("handler.%N(this, navigator)", navigationHandlerHandle)
            .build()
    }
}
