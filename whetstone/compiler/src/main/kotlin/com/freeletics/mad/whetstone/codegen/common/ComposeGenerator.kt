package com.freeletics.mad.whetstone.codegen.common

import com.freeletics.mad.whetstone.BaseData
import com.freeletics.mad.whetstone.ComposeData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.asComposeState
import com.freeletics.mad.whetstone.codegen.util.composable
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.launch
import com.freeletics.mad.whetstone.codegen.util.optInAnnotation
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.rememberCoroutineScope
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE

internal val Generator<out BaseData>.composableName
    get() = "Whetstone${data.baseName}"

internal class ComposeGenerator(
    override val data: ComposeData,
) : Generator<ComposeData>() {

    internal fun generate(): FunSpec {
        val composableParameterProperties = data.composableParameter.map { it.propertyName }
        val parameterString = composableParameterProperties.joinToString().apply {
            if(isNotBlank()) plus(", ")
        }

        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addAnnotation(optInAnnotation())
            .addModifiers(PRIVATE)
            .addParameter("component", retainedComponentClassName)
            .addStatement("val providedValues = component.%L", providedValueSetPropertyName)
            .beginControlFlow("%T(*providedValues.toTypedArray()) {", compositionLocalProvider)
            .apply {
                composableParameterProperties.forEach { propertyName ->
                    addStatement("val %L = component.%L", propertyName, propertyName)
                }
            }
            .addStatement("val stateMachine = component.%L", data.stateMachine.propertyName)
            .addStatement("val state = stateMachine.%M()", asComposeState)
            .addStatement("val currentState = state.value")
            .beginControlFlow("if (currentState != null)")
            .addStatement("val scope = %M()", rememberCoroutineScope)
            .beginControlFlow("%L(%LcurrentState) { action ->", data.baseName, parameterString)
            // dispatch: external method
            .addStatement("scope.%M { stateMachine.dispatch(action) }", launch)
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .build()
    }
}
