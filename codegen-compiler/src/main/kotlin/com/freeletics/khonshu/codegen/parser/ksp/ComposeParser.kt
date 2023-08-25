package com.freeletics.khonshu.codegen.parser.ksp

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.compose.DestinationType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal fun KSFunctionDeclaration.toComposeScreenData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeScreenData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    return ComposeScreenData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        navigation = null,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}

internal fun KSFunctionDeclaration.toComposeScreenDestinationData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeScreenData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    val navigation = Navigation.Compose(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScope.extendsBaseRoute(resolver),
        destinationType = DestinationType.valueOf(annotation.destinationType),
        destinationScope = annotation.destinationScope,
    )

    return ComposeScreenData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = this.getParameterWithType(stateParameter),
        sendActionParameter = this.getParameterWithType(actionParameter),
    )
}
