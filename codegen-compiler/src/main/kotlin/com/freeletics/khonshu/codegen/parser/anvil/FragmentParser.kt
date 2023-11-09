package com.freeletics.khonshu.codegen.parser.anvil

import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.codegen.util.composeFragmentDestinationFqName
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.asClassName

internal fun TopLevelFunctionReference.toComposeFragmentDestinationData(): ComposeFragmentData? {
    val annotation = findAnnotation(composeFragmentDestinationFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val (stateParameter, actionParameter) = stateMachine.stateMachineParameters()

    val navigation = Navigation.Fragment(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScopeReference.extendsBaseRoute(),
        overlay = annotation.routeReference.extendsOverlay(),
        destinationScope = annotation.destinationScope,
    )

    return ComposeFragmentData(
        baseName = "Fragment$name",
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        fragmentBaseClass = annotation.fragmentBaseClass,
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}
