package com.freeletics.mad.whetstone.codegen.compose

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.codegen.common.providedValueSetPropertyName
import com.freeletics.mad.whetstone.codegen.common.viewModelClassName
import com.freeletics.mad.whetstone.codegen.common.viewModelComponentName
import com.freeletics.mad.whetstone.codegen.util.Generator
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.bundle
import com.freeletics.mad.whetstone.codegen.util.asState
import com.freeletics.mad.whetstone.codegen.util.composable
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.launch
import com.freeletics.mad.whetstone.codegen.util.launchedEffect
import com.freeletics.mad.whetstone.codegen.util.localOnBackPressedDispatcherOwner
import com.freeletics.mad.whetstone.codegen.util.navController
import com.freeletics.mad.whetstone.codegen.util.navigationHandlerHandle
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
            .addParameter("navController", navController)
            .beginControlFlow("val viewModelProvider = %M<%T>(%T::class) { dependencies, handle -> ", rememberViewModelProvider, data.dependencies, data.parentScope)
            // currentBackStackEntry: external method
            // arguments: external method
            .addStatement("val arguments = navController.currentBackStackEntry!!.arguments ?: %T.EMPTY", bundle)
            .addStatement("%T(dependencies, handle, arguments)", viewModelClassName)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("val component = viewModel.%L", viewModelComponentName)
            .addCode("\n")
            .addCode(composableNavigationSetup(disableNavigation))
            .addStatement("val providedValues = component.%L", providedValueSetPropertyName)
            .beginControlFlow("%T(*providedValues.toTypedArray()) {", compositionLocalProvider)
            .addStatement("val stateMachine = component.%L", data.stateMachine.propertyName)
            .addStatement("val state = stateMachine.%M()", asState)
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
        if (data.navigation == null || disableNavigation) {
            return CodeBlock.of("")
        }

        return CodeBlock.builder()
            .addStatement("val onBackPressedDispatcher = %T.current!!.onBackPressedDispatcher", localOnBackPressedDispatcherOwner)
            .beginControlFlow("%M(navController, onBackPressedDispatcher, component) {", launchedEffect)
            .addStatement("val handler = component.%L", data.navigation!!.navigationHandler.propertyName)
            .addStatement("val navigator = component.%L", data.navigation!!.navigator.propertyName)
            .addStatement("handler.%N(this, navController, onBackPressedDispatcher, navigator)", navigationHandlerHandle)
            .endControlFlow()
            .add("\n")
            .build()
    }
}
