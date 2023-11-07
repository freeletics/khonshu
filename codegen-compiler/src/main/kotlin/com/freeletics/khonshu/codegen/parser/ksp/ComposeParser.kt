package com.freeletics.khonshu.codegen.parser.ksp

import com.freeletics.khonshu.codegen.ComposeScreenData
import com.freeletics.khonshu.codegen.NavHostActivityData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.parser.anvil.extendsOverlay
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

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
        overlay = annotation.route.extendsOverlay(resolver),
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

internal fun KSFunctionDeclaration.toNavHostActivityData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): NavHostActivityData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    val navHostParameter = navHostParameter(logger) ?: return null

    return NavHostActivityData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        activityBaseClass = annotation.activityBaseClass,
        navHostParameter = navHostParameter,
        composableParameter = getInjectedParameters(stateParameter, actionParameter, navHostParameter.typeName),
        stateParameter = getParameterWithType(stateParameter),
        sendActionParameter = getParameterWithType(actionParameter),
    )
}
