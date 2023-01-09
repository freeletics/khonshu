package com.freeletics.mad.whetstone.codegen.compose

import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.common.composableName
import com.freeletics.mad.whetstone.codegen.common.retainedComponentFactoryCreateName
import com.freeletics.mad.whetstone.codegen.common.retainedParentComponentClassName
import com.freeletics.mad.whetstone.codegen.common.retainedParentComponentGetterName
import com.freeletics.mad.whetstone.codegen.util.asParameter
import com.freeletics.mad.whetstone.codegen.util.composable
import com.freeletics.mad.whetstone.codegen.util.composeNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.navEventNavigator
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.rememberComponent
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

internal class ComposeScreenGenerator(
    override val data: ComposeScreenData,
) : Generator<ComposeScreenData>() {

    internal fun generate(): FunSpec {
        val parameter = data.navigation.asParameter()
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation())
            .addParameter(parameter)
            .also {
                if (data.navigation != null) {
                    it.beginControlFlow("val component = %M(%T::class, %T::class, %N) { parentComponent: %T, savedStateHandle, %N ->",
                        rememberComponent, data.parentScope, data.navigation.destinationScope,
                        parameter, retainedParentComponentClassName, parameter)
                } else {
                    it.beginControlFlow("val component = %M(%T::class, %N) { parentComponent: %T, savedStateHandle, %N ->",
                        rememberComponent, data.parentScope, parameter,
                        retainedParentComponentClassName, parameter)
                }
            }
            .addStatement("parentComponent.%L().%L(savedStateHandle, %N)",
                retainedParentComponentGetterName, retainedComponentFactoryCreateName, parameter)
            .endControlFlow()
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
