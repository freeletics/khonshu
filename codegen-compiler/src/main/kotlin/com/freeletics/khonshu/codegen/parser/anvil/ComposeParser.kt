package com.freeletics.khonshu.codegen.parser.anvil

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.codegen.util.navDestinationFqName
import com.freeletics.khonshu.codegen.codegen.util.navHostActivityFqName
import com.freeletics.khonshu.codegen.parser.ksp.extendsBaseRoute
import com.freeletics.khonshu.codegen.parser.ksp.extendsOverlay
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.asClassName

internal fun TopLevelFunctionReference.toComposeScreenDestinationData(): ComposeScreenData? {
    val annotation = findAnnotation(navDestinationFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val (stateParameter, actionParameter) = stateMachine.stateMachineParameters()

    val navigation = Navigation.Compose(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScopeReference.extendsBaseRoute(),
        overlay = annotation.routeReference.extendsOverlay(),
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

internal fun TopLevelFunctionReference.toNavHostActivityData(): NavHostActivityData? {
    val annotation = findAnnotation(navHostActivityFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val (stateParameter, actionParameter) = stateMachine.stateMachineParameters()

    val navHostParameter = navHostParameter()

    return NavHostActivityData(
        baseName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        activityBaseClass = annotation.activityBaseClass,
        navHostParameter = navHostParameter,
        composableParameter = getInjectedParameters(stateParameter, actionParameter, navHostParameter.typeName),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}
