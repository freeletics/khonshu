package com.freeletics.khonshu.codegen.parser.anvil

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.codegen.util.codegenComposeDestinationFqName
import com.freeletics.khonshu.codegen.codegen.util.composeFqName
import com.freeletics.khonshu.codegen.compose.DestinationType
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.asClassName

internal fun TopLevelFunctionReference.toComposeScreenData(): ComposeScreenData? {
    val annotation = findAnnotation(composeFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val (stateParameter, actionParameter) = stateMachine.stateMachineParameters()

    return ComposeScreenData(
        baseName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        navigation = null,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}

internal fun TopLevelFunctionReference.toComposeScreenDestinationData(): ComposeScreenData? {
    val annotation = findAnnotation(codegenComposeDestinationFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val (stateParameter, actionParameter) = stateMachine.stateMachineParameters()

    val navigation = Navigation.Compose(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScopeReference.extendsBaseRoute(),
        destinationType = DestinationType.valueOf(annotation.destinationType),
        destinationScope = annotation.destinationScope,
    )

    return ComposeScreenData(
        baseName = name,
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}
