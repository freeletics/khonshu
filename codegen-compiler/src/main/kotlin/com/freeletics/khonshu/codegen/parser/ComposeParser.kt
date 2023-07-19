package com.freeletics.khonshu.codegen.parser

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.codegen.util.codegenComposeDestinationFqName
import com.freeletics.khonshu.codegen.codegen.util.composeFqName
import com.freeletics.khonshu.codegen.codegen.util.stateMachineFqName
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.asClassName

internal fun TopLevelFunctionReference.toComposeScreenData(): ComposeScreenData? {
    val annotation = findAnnotation(composeFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val stateMachineSuperType = stateMachine.superTypeReference(stateMachineFqName)
    val stateParameter = stateMachine.stateMachineStateParameter(stateMachineSuperType)
    val actionParameter = stateMachine.stateMachineActionFunctionParameter(stateMachineSuperType)

    return ComposeScreenData(
        baseName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        navigation = null,
        composableParameter = getComposeParameters(stateParameter, actionParameter),
        stateParameter = getStateParameter(stateParameter),
        sendActionParameter = getSendActionParameter(actionParameter),
    )
}

internal fun TopLevelFunctionReference.toComposeScreenDestinationData(): ComposeScreenData? {
    val annotation = findAnnotation(codegenComposeDestinationFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val stateMachineSuperType = stateMachine.superTypeReference(stateMachineFqName)
    val stateParameter = stateMachine.stateMachineStateParameter(stateMachineSuperType)
    val actionParameter = stateMachine.stateMachineActionFunctionParameter(stateMachineSuperType)

    val navigation = Navigation.Compose(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScopeReference.extendsBaseRoute(),
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope,
    )

    return ComposeScreenData(
        baseName = name,
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        navigation = navigation,
        composableParameter = getComposeParameters(stateParameter, actionParameter),
        stateParameter = getStateParameter(stateParameter),
        sendActionParameter = getSendActionParameter(actionParameter),
    )
}
