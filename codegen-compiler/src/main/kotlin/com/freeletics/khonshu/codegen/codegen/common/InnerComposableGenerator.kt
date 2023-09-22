package com.freeletics.khonshu.codegen.codegen.common

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.ComposeData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.codegen.Generator
import com.freeletics.khonshu.codegen.codegen.util.asComposeState
import com.freeletics.khonshu.codegen.codegen.util.composable
import com.freeletics.khonshu.codegen.codegen.util.launch
import com.freeletics.khonshu.codegen.codegen.util.navHostParameter
import com.freeletics.khonshu.codegen.codegen.util.optInAnnotation
import com.freeletics.khonshu.codegen.codegen.util.propertyName
import com.freeletics.khonshu.codegen.codegen.util.remember
import com.freeletics.khonshu.codegen.codegen.util.rememberCoroutineScope
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE

internal val Generator<out BaseData>.composableName
    get() = "Khonshu${data.baseName}"

/**
 * The inner Composable is used for both Fragments and pure compose mode.
 * Receives the component, will do the StateMachine set up and then calls
 * the annotated composable with all required parameters.
 */
internal class InnerComposableGenerator(
    override val data: ComposeData,
) : Generator<ComposeData>() {

    internal fun generate(): FunSpec {
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation())
            .addModifiers(PRIVATE)
            .addParameter("component", retainedComponentClassName)
            .also {
                if (data is NavHostActivityData) {
                    it.addParameter(navHostParameter(data.navHostParameter))
                }
            }
            .addCode(composableBlock())
            .build()
    }

    private fun composableBlock(): CodeBlock {
        return CodeBlock.builder()
            .apply {
                data.composableParameter.forEach { parameter ->
                    addStatement("val %L = %M { component.%L }", parameter.name, remember, parameter.name)
                }
            }
            .addStatement("val stateMachine = %M { component.%L }", remember, data.stateMachine.propertyName)
            .addStatement("val state = stateMachine.%M()", asComposeState)
            .addStatement("val currentState = state.value")
            .beginControlFlow("if (currentState != null)")
            .apply {
                if (data.sendActionParameter != null) {
                    addStatement("val scope = %M()", rememberCoroutineScope)
                }
            }
            .addStatement("%L(", data.baseName.removePrefix("Fragment"))
            .apply {
                data.composableParameter.forEach { parameter ->
                    addStatement("  %L = %L,", parameter.name, parameter.name)
                }
                if (data.stateParameter != null) {
                    addStatement("  %L = currentState,", data.stateParameter!!.name)
                }
                if (data.sendActionParameter != null) {
                    // dispatch: external method
                    addStatement(
                        "  %L = { scope.%M { stateMachine.dispatch(it) } },",
                        data.sendActionParameter!!.name,
                        launch,
                    )
                }
                if (data is NavHostActivityData) {
                    addStatement(
                        "  %L = %L,",
                        data.navHostParameter.name,
                        data.navHostParameter.name,
                    )
                }
            }
            .addStatement(")")
            .endControlFlow()
            .build()
    }
}
