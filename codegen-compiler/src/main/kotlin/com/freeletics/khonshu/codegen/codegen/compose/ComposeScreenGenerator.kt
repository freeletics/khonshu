package com.freeletics.khonshu.codegen.codegen.compose

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.common.composableName
import com.freeletics.khonshu.codegen.codegen.common.retainedComponentFactoryCreateName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentClassName
import com.freeletics.khonshu.codegen.codegen.common.retainedParentComponentGetterName
import com.freeletics.khonshu.codegen.codegen.util.asParameter
import com.freeletics.khonshu.codegen.codegen.util.composable
import com.freeletics.khonshu.codegen.codegen.util.composeNavigationHandler
import com.freeletics.khonshu.codegen.codegen.util.navEventNavigator
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.codegen.util.propertyName
import com.freeletics.khonshu.codegen.codegen.util.rememberComponent
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

internal class ComposeScreenGenerator(
    override val data: ComposeScreenData,
) : Generator<ComposeScreenData>() {

    internal fun generate(): FunSpec {
        val parameter = data.navigation.asParameter()
        val innerParameterName = "${parameter.name}ForComponent"
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation())
            .addParameter(parameter)
            .also {
                if (data.navigation != null) {
                    it.beginControlFlow(
                        "val component = %M(%T::class, %T::class, %N) { parentComponent: %T, savedStateHandle, %L ->",
                        rememberComponent,
                        data.parentScope,
                        data.navigation.destinationScope,
                        parameter,
                        retainedParentComponentClassName,
                        innerParameterName,
                    )
                } else {
                    it.beginControlFlow(
                        "val component = %M(%T::class, %N) { parentComponent: %T, savedStateHandle, %L ->",
                        rememberComponent,
                        data.parentScope,
                        parameter,
                        retainedParentComponentClassName,
                        innerParameterName,
                    )
                }
            }
            .addStatement(
                "parentComponent.%L().%L(savedStateHandle, %L)",
                retainedParentComponentGetterName,
                retainedComponentFactoryCreateName,
                innerParameterName,
            )
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