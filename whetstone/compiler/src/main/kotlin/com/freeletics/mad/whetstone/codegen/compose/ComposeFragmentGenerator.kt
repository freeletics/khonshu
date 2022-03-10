package com.freeletics.mad.whetstone.codegen.compose

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.composeView
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.disposeOnLifecycleDestroyed
import com.freeletics.mad.whetstone.codegen.util.fragmentConverter
import com.freeletics.mad.whetstone.codegen.util.fragmentNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.fragmentViewModelProvider
import com.freeletics.mad.whetstone.codegen.util.layoutInflater
import com.freeletics.mad.whetstone.codegen.util.layoutParams
import com.freeletics.mad.whetstone.codegen.util.localWindowInsets
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.view
import com.freeletics.mad.whetstone.codegen.util.viewGroup
import com.freeletics.mad.whetstone.codegen.util.viewWindowInsetsObserver
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal class ComposeFragmentGenerator(
    override val data: ComposeFragmentData,
) : Generator<ComposeFragmentData>() {

    private val composeFragmentClassName = ClassName("${data.baseName}Fragment")

    internal fun generate(): TypeSpec {
        val argumentsParameter = data.navigation.asParameter()
        return TypeSpec.classBuilder(composeFragmentClassName)
            .addAnnotation(optInAnnotation())
            .superclass(data.fragmentBaseClass)
            .addFunction(composeOnCreateViewFun(argumentsParameter))
            .apply {
                if (data.navigation != null) {
                    addProperty(navigationSetupProperty())
                    addFunction(setupNavigationFun(argumentsParameter))
                }
            }
            .build()
    }


    private fun composeOnCreateViewFun(argumentsParameter: ParameterSpec): FunSpec {
        return FunSpec.builder("onCreateView")
            .addModifiers(OVERRIDE)
            .addParameter("inflater", layoutInflater)
            .addParameter("container", viewGroup.copy(nullable = true))
            .addParameter("savedInstanceState", bundle.copy(nullable = true))
            .returns(view)
            .addCode("val %N = ", argumentsParameter)
            .addCode(data.navigation.fragmentConverter())
            .addCode("\n")
            .apply {
                if (data.navigation != null) {
                    beginControlFlow("if (!%L)", navigationSetupPropertyName)
                    addStatement("%L = true", navigationSetupPropertyName)
                    addStatement("%L(%N)", navigationSetupFunctionName, argumentsParameter)
                    endControlFlow()
                    addCode("\n")
                }
            }
            // requireContext: external method
            .beginControlFlow("return %T(requireContext()).apply {", composeView)
            // setViewCompositionStrategy: external method
            // viewLifecycleOwner: external method
            .addStatement("setViewCompositionStrategy(%T(viewLifecycleOwner))", disposeOnLifecycleDestroyed)
            .addCode("\n")
            .apply {
                if (data.enableInsetHandling) {
                    addStatement("layoutParams = %1T(%1T.MATCH_PARENT, %1T.MATCH_PARENT)", layoutParams)
                    addStatement("val observer = %T(this)", viewWindowInsetsObserver)
                    // start: external method
                    addStatement("val windowInsets = observer.start()")
                    addCode("\n")
                }
            }
            // setContent: external method
            .beginControlFlow("setContent {")
            .apply {
                if (data.enableInsetHandling) {
                    beginControlFlow("%T(%T provides windowInsets) {", compositionLocalProvider, localWindowInsets)
                }
            }
            // requireArguments: external method
            .addStatement("%L(%N)", composableName, argumentsParameter)
            .apply {
                if (data.enableInsetHandling) {
                    endControlFlow()
                }
            }
            .endControlFlow()
            .endControlFlow()
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

    private fun setupNavigationFun(argumentsParameter: ParameterSpec): FunSpec {
        return FunSpec.builder("setupNavigation")
            .addModifiers(PRIVATE)
            .addParameter(argumentsParameter)
            .beginControlFlow("val viewModelProvider = %M<%T>(this, %T::class) { dependencies, handle -> ", fragmentViewModelProvider, data.dependencies, data.parentScope)
            // arguments: external method
            .addStatement("%T(dependencies, handle, %N)", viewModelClassName, argumentsParameter)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("val component = viewModel.%L", viewModelComponentName)
            .addCode("\n")
            .addStatement("val navigator = component.%L", data.navigation!!.navigator.propertyName)
            .addStatement("%M(this, navigator)", fragmentNavigationHandler)
            .build()
    }
}
