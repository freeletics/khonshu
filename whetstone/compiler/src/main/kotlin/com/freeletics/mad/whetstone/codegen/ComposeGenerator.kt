package com.freeletics.mad.whetstone.codegen

import com.freeletics.mad.whetstone.Data
import com.freeletics.mad.whetstone.Extra
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

internal val Generator.composableName get() = "${data.baseName}Screen"

internal class ComposeGenerator(
    override val data: Data,
) : Generator() {

    internal fun generate(): FunSpec {
        val withFragment = (data.extra as Extra.Compose).withFragment
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation())
            .addParameter("navController", navController)
            .apply {
                if (data.navigation != null) {
                    if (withFragment) {
                        addParameter("fragment", fragment)
                    } else {
                        addParameter("onBackPressedDispatcher", onBackPressedDispatcher)
                    }
                }
            }
            .beginControlFlow("val viewModelProvider = %M<%T>(%T::class) { dependencies, handle -> ", rememberViewModelProvider, data.dependencies, data.parentScope)
            // currentBackStackEntry: external method
            // arguments: external method
            .addStatement("val arguments = navController.currentBackStackEntry!!.arguments ?: %T.EMPTY", bundle)
            .addStatement("%T(dependencies, handle, arguments)", viewModelClassName)
            .endControlFlow()
            .addStatement("val viewModel = viewModelProvider[%T::class.java]", viewModelClassName)
            .addStatement("val component = viewModel.%L", viewModelComponentName)
            .addCode("\n")
            .addCode(composableNavigationSetup(withFragment))
            .addStatement("val stateMachine = component.%L", data.stateMachine.propertyName)
            .addStatement("val state = stateMachine.state.%M()", collectAsState)
            .addStatement("val scope = %M()", rememberCoroutineScope)
            .beginControlFlow("%L(state.value) { action ->", data.baseName)
            // dispatch: external method
            .addStatement("scope.%M { stateMachine.dispatch(action) }", launch)
            .endControlFlow()
            .build()
    }

    private fun composableNavigationSetup(withFragment: Boolean): CodeBlock {
        if (data.navigation == null) {
            return CodeBlock.of("")
        }

        val parameters = if (withFragment) {
            "fragment"
        } else {
            "navController, onBackPressedDispatcher"
        }

        return CodeBlock.builder()
            .beginControlFlow("%M($parameters, component) {", launchedEffect)
            .addStatement("val handler = component.%L", data.navigation.navigationHandler.propertyName)
            .addStatement("val navigator = component.%L", data.navigation.navigator.propertyName)
            .addStatement("handler.%N(this, $parameters, navigator)", navigationHandlerHandle)
            .endControlFlow()
            .add("\n")
            .build()
    }
}
