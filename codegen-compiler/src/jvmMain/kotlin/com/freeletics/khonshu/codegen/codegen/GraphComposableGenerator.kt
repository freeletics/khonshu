package com.freeletics.khonshu.codegen.codegen

import com.freeletics.khonshu.codegen.BaseData
import com.freeletics.khonshu.codegen.HostActivityData
import com.freeletics.khonshu.codegen.util.asComposeState
import com.freeletics.khonshu.codegen.util.composable
import com.freeletics.khonshu.codegen.util.khonshuStateMachine
import com.freeletics.khonshu.codegen.util.launch
import com.freeletics.khonshu.codegen.util.navHostParameter
import com.freeletics.khonshu.codegen.util.optIn
import com.freeletics.khonshu.codegen.util.produceStateMachine
import com.freeletics.khonshu.codegen.util.propertyName
import com.freeletics.khonshu.codegen.util.remember
import com.freeletics.khonshu.codegen.util.rememberCoroutineScope
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.asClassName
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal val Generator<out BaseData>.composableName
    get() = "Khonshu${data.baseName}"

/**
 * The inner Composable is used for both NavDestinations and Activities.
 * Receives the graph, will do the StateMachine set up and then calls
 * the annotated composable with all required parameters.
 */
internal class GraphComposableGenerator(
    override val data: BaseData,
) : Generator<BaseData>() {
    internal fun generate(): FunSpec {
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(
                if (data.stateMachineClass == khonshuStateMachine) {
                    optIn()
                } else {
                    optIn(
                        ExperimentalCoroutinesApi::class.asClassName(),
                    )
                },
            )
            .addModifiers(PRIVATE)
            .addParameter("graph", graphClassName)
            .also {
                if (data is HostActivityData) {
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
                    addStatement("val %L = %M { graph.%L }", parameter.name, remember, parameter.name)
                }
            }
            .apply {
                if (data.stateMachineClass == khonshuStateMachine) {
                    addStatement("val stateMachine = %M { graph.%L }", remember, data.stateMachine.propertyName)
                    if (data.sendActionParameter != null) {
                        addStatement("val scope = %M()", rememberCoroutineScope)
                            .beginControlFlow(
                                "val sendAction: %T = %M(stateMachine, scope)",
                                data.sendActionParameter!!.typeName,
                                remember,
                            )
                            .addStatement("{ scope.%M { stateMachine.dispatch(it) } }", launch)
                            .endControlFlow()
                    }
                    addStatement("val state = stateMachine.%M()", asComposeState)
                    addStatement("val currentState = state.value")
                    beginControlFlow("if (currentState != null)")
                } else {
                    addStatement("val stateMachineFactory = %M { graph.%L }", remember, data.stateMachine.propertyName)
                    addStatement("val stateMachine = stateMachineFactory.%M()", produceStateMachine)
                    if (data.stateParameter != null) {
                        addStatement("val currentState = stateMachine.state.value")
                    }
                    if (data.sendActionParameter != null) {
                        addStatement("val sendAction = stateMachine.dispatchAction")
                    }
                }
            }
            .addStatement("%L(", data.baseName)
            .apply {
                data.composableParameter.forEach { parameter ->
                    addStatement("  %L = %L,", parameter.name, parameter.name)
                }
                if (data.stateParameter != null) {
                    addStatement("  %L = currentState,", data.stateParameter!!.name)
                }
                if (data.sendActionParameter != null) {
                    addStatement("  %L = sendAction,", data.sendActionParameter!!.name)
                }
                if (data is HostActivityData) {
                    addStatement(
                        "  %L = %L,",
                        data.navHostParameter.name,
                        data.navHostParameter.name,
                    )
                }
            }
            .addStatement(")")
            .apply {
                if (data.stateMachineClass == khonshuStateMachine) {
                    endControlFlow()
                }
            }
            .build()
    }
}
