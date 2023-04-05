package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.codegen.util.composeFqName
import com.freeletics.mad.whetstone.codegen.util.stateMachineFqName
import com.freeletics.mad.whetstone.codegen.util.whetstoneComposeScreenDestinationFqName
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
        navEntryData = null,
        composableParameter = getComposeParameters(stateParameter, actionParameter),
        stateParameter = getStateParameter(stateParameter),
        sendActionParameter = getSendActionParameter(actionParameter),
    )
}

internal fun TopLevelFunctionReference.toComposeScreenDestinationData(): ComposeScreenData? {
    val annotation = findAnnotation(whetstoneComposeScreenDestinationFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val stateMachineSuperType = stateMachine.superTypeReference(stateMachineFqName)
    val stateParameter = stateMachine.stateMachineStateParameter(stateMachineSuperType)
    val actionParameter = stateMachine.stateMachineActionFunctionParameter(stateMachineSuperType)

    val navigation = Navigation.Compose(
        route = annotation.route,
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
        navEntryData = navEntryData(navigation),
        composableParameter = getComposeParameters(stateParameter, actionParameter),
        stateParameter = getStateParameter(stateParameter),
        sendActionParameter = getSendActionParameter(actionParameter),
    )
}
