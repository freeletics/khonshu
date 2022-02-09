package com.freeletics.mad.whetstone.codegen.compose

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.codegen.common.providedValueSetPropertyName
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.asComposeState
import com.freeletics.mad.whetstone.codegen.util.composable
import com.freeletics.mad.whetstone.codegen.util.composeNavigationHandler
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.launch
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.rememberCoroutineScope
import com.freeletics.mad.whetstone.codegen.util.rememberViewModelProvider
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

internal val Generator<out CommonData>.composableName get() = "${data.baseName}Screen"

internal class ComposeGenerator(
    override val data: CommonData,
) : Generator<CommonData>() {

    internal fun generate(disableNavigation: Boolean): FunSpec {
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation())
            .addParameter("arguments", bundle)
            .beginControlFlow("val viewModelProvider = %M<%T>(%T::class) { dependencies, handle -> ", rememberViewModelProvider, data.dependencies, data.parentScope)
            .addStatement("%T(dependencies, handle, arguments)", viewModelClassName)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("val component = viewModel.%L", viewModelComponentName)
            .addCode("\n")
            .addCode(composableNavigationSetup(disableNavigation))
            .addStatement("val providedValues = component.%L", providedValueSetPropertyName)
            .beginControlFlow("%T(*providedValues.toTypedArray()) {", compositionLocalProvider)
            .addStatement("val stateMachine = component.%L", data.stateMachine.propertyName)
            .addStatement("val state = stateMachine.%M()", asComposeState)
            .addStatement("val currentState = state.value")
            .beginControlFlow("if (currentState != null)")
            .addStatement("val scope = %M()", rememberCoroutineScope)
            .beginControlFlow("%L(currentState) { action ->", data.baseName)
            // dispatch: external method
            .addStatement("scope.%M { stateMachine.dispatch(action) }", launch)
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .build()
    }

    private fun composableNavigationSetup(disableNavigation: Boolean): CodeBlock {
        val navigator = data.navigator
        if (navigator == null || disableNavigation) {
            return CodeBlock.of("")
        }

        return CodeBlock.builder()
            .addStatement("val navigator = component.%L", navigator.propertyName)
            .addStatement("%M(navigator)", composeNavigationHandler)
            .add("\n")
            .build()
    }
}
