package com.freeletics.khonshu.codegen.parser.ksp

import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.Navigation
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal fun KSFunctionDeclaration.toComposeFragmentDestinationData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeFragmentData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    val navigation = Navigation.Fragment(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScope.extendsBaseRoute(resolver),
        overlay = annotation.route.extendsOverlay(resolver),
        destinationScope = annotation.destinationScope,
    )

    return ComposeFragmentData(
        baseName = "Fragment" + simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass,
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = this.getParameterWithType(stateParameter),
        sendActionParameter = this.getParameterWithType(actionParameter),
    )
}
