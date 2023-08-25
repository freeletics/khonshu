package com.freeletics.khonshu.codegen.parser.ksp

import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.RendererFragmentData
import com.freeletics.khonshu.codegen.fragment.DestinationType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal fun KSClassDeclaration.toRendererFragmentData(
    annotation: KSAnnotation,
    logger: KSPLogger,
): RendererFragmentData? {
    return RendererFragmentData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass,
        factory = findRendererFactory(logger) ?: return null,
        navigation = null,
    )
}

internal fun KSClassDeclaration.toRendererFragmentDestinationData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): RendererFragmentData? {
    val navigation = Navigation.Fragment(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScope.extendsBaseRoute(resolver),
        destinationType = DestinationType.valueOf(annotation.destinationType),
        destinationScope = annotation.destinationScope,
    )

    return RendererFragmentData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass,
        factory = findRendererFactory(logger) ?: return null,
        navigation = navigation,
    )
}

internal fun KSFunctionDeclaration.toComposeFragmentData(
    annotation: KSAnnotation,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeFragmentData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    return ComposeFragmentData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass,
        navigation = null,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = this.getParameterWithType(stateParameter),
        sendActionParameter = this.getParameterWithType(actionParameter),
    )
}

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
        destinationType = DestinationType.valueOf(annotation.destinationType),
        destinationScope = annotation.destinationScope,
    )

    return ComposeFragmentData(
        baseName = simpleName.asString(),
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
