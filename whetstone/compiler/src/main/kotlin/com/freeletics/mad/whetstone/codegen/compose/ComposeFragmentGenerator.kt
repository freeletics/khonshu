package com.freeletics.mad.whetstone.codegen.compose

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.composeView
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.disposeOnLifecycleDestroyed
import com.freeletics.mad.whetstone.codegen.util.layoutInflater
import com.freeletics.mad.whetstone.codegen.util.layoutParams
import com.freeletics.mad.whetstone.codegen.util.localWindowInsets
import com.freeletics.mad.whetstone.codegen.util.navigationHandlerHandle
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.view
import com.freeletics.mad.whetstone.codegen.util.viewGroup
import com.freeletics.mad.whetstone.codegen.util.fragmentViewModelProvider
import com.freeletics.mad.whetstone.codegen.util.viewWindowInsetsObserver
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal class ComposeFragmentGenerator(
    override val data: ComposeFragmentData,
) : Generator<ComposeFragmentData>() {

    private val composeFragmentClassName = ClassName("${data.baseName}Fragment")

    internal fun generate(): TypeSpec {
        return TypeSpec.classBuilder(composeFragmentClassName)
            .addAnnotation(optInAnnotation())
            .superclass(data.fragmentBaseClass)
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
            .addStatement("%L(requireArguments())", composableName)
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

    private fun setupNavigationFun(): FunSpec {
        return FunSpec.builder("setupNavigation")
            .addModifiers(PRIVATE)
            .beginControlFlow("val viewModelProvider = %M<%T>(this, %T::class) { dependencies, handle -> ", fragmentViewModelProvider, data.dependencies, data.parentScope)
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
