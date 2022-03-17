package com.freeletics.mad.whetstone.codegen.compose

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.codegen.common.providedValueSetPropertyName
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.common.composableName
import com.freeletics.mad.whetstone.codegen.common.retainedComponentClassName
import com.freeletics.mad.whetstone.codegen.util.asComposeState
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.composable
import com.freeletics.mad.whetstone.codegen.util.composeNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.launch
import com.freeletics.mad.whetstone.codegen.util.navEventNavigator
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.rememberCoroutineScope
import com.freeletics.mad.whetstone.codegen.util.rememberViewModelProvider
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE

internal class ComposeScreenGenerator(
    override val data: CommonData,
) : Generator<CommonData>() {

    internal fun generate(): FunSpec {
        val parameter = data.navigation.asParameter()
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation())
            .addParameter(parameter)
            .beginControlFlow("val viewModelProvider = %M<%T>(%T::class) { dependencies, handle -> ",
                rememberViewModelProvider, data.dependencies, data.parentScope)
            .addStatement("%T(dependencies, handle, %N)", viewModelClassName, parameter)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("val component = viewModel.%L", viewModelComponentName)
            .addCode("\n")
            .addCode(composableNavigationSetup())
            .addStatement("%L(component)", composableName)
            .build()

    }

    private fun composableNavigationSetup(): CodeBlock {
        if (data.navigation == null) {
            return CodeBlock.of("")
        }

        return CodeBlock.builder()
            .addStatement("%M(component.%L)", composeNavigationHandler, navEventNavigator.propertyName)
            .add("\n")
            .build()
    }
}
