package com.freeletics.mad.whetstone.codegen.common

import com.freeletics.mad.whetstone.CommonData
import com.freeletics.mad.whetstone.codegen.Generator
import com.freeletics.mad.whetstone.codegen.util.asComposeState
import com.freeletics.mad.whetstone.codegen.util.composable
import com.freeletics.mad.whetstone.codegen.util.compositionLocalProvider
import com.freeletics.mad.whetstone.codegen.util.launch
import com.freeletics.mad.whetstone.codegen.util.propertyName
import com.freeletics.mad.whetstone.codegen.util.rememberCoroutineScope
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE

internal val Generator<out CommonData>.composableName
    get() = "${data.baseName}Screen"

internal class ComposeGenerator(
    override val data: CommonData,
) : Generator<CommonData>() {

    internal fun generate(): FunSpec {
        return FunSpec.builder(composableName)
            .addAnnotation(composable)
            .addModifiers(PRIVATE)
            .addParameter("component", retainedComponentClassName)
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
}
