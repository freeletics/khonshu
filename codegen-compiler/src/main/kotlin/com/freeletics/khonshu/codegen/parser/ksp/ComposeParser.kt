package com.freeletics.khonshu.codegen.parser.ksp

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.compose.ComposeDestination
import com.freeletics.khonshu.codegen.compose.ComposeScreen
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.asClassName

internal fun KSFunctionDeclaration.toComposeScreenData(
    annotation: ComposeScreen,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeScreenData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    return ComposeScreenData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope.asClassName(),
        parentScope = annotation.parentScope.asClassName(),
        stateMachine = annotation.stateMachine.asClassName(),
        navigation = null,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}

internal fun KSFunctionDeclaration.toComposeScreenDestinationData(
    annotation: ComposeDestination,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeScreenData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    val navigation = Navigation.Compose(
        route = annotation.route.asClassName(),
        parentScopeIsRoute = annotation.parentScope.extendsBaseRoute(resolver),
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope.asClassName(),
    )

    return ComposeScreenData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.route.asClassName(),
        parentScope = annotation.parentScope.asClassName(),
        stateMachine = annotation.stateMachine.asClassName(),
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = this.getParameterWithType(stateParameter),
        sendActionParameter = this.getParameterWithType(actionParameter),
    )
}
