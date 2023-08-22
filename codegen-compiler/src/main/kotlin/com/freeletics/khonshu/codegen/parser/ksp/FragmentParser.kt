package com.freeletics.khonshu.codegen.parser.ksp

import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.RendererFragmentData
import com.freeletics.khonshu.codegen.fragment.ComposeDestination
import com.freeletics.khonshu.codegen.fragment.ComposeFragment
import com.freeletics.khonshu.codegen.fragment.RendererDestination
import com.freeletics.khonshu.codegen.fragment.RendererFragment
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.asClassName

internal fun KSClassDeclaration.toRendererFragmentData(
    annotation: RendererFragment,
    logger: KSPLogger,
): RendererFragmentData? {
    return RendererFragmentData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope.asClassName(),
        parentScope = annotation.parentScope.asClassName(),
        stateMachine = annotation.stateMachine.asClassName(),
        fragmentBaseClass = annotation.fragmentBaseClass.asClassName(),
        factory = findRendererFactory(logger) ?: return null,
        navigation = null,
    )
}

internal fun KSClassDeclaration.toRendererFragmentDestinationData(
    annotation: RendererDestination,
    resolver: Resolver,
    logger: KSPLogger,
): RendererFragmentData? {
    val navigation = Navigation.Fragment(
        route = annotation.route.asClassName(),
        parentScopeIsRoute = annotation.parentScope.extendsBaseRoute(resolver),
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope.asClassName(),
    )

    return RendererFragmentData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.route.asClassName(),
        parentScope = annotation.parentScope.asClassName(),
        stateMachine = annotation.stateMachine.asClassName(),
        fragmentBaseClass = annotation.fragmentBaseClass.asClassName(),
        factory = findRendererFactory(logger) ?: return null,
        navigation = navigation,
    )
}

internal fun KSFunctionDeclaration.toComposeFragmentData(
    annotation: ComposeFragment,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeFragmentData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    return ComposeFragmentData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.scope.asClassName(),
        parentScope = annotation.parentScope.asClassName(),
        stateMachine = annotation.stateMachine.asClassName(),
        fragmentBaseClass = annotation.fragmentBaseClass.asClassName(),
        navigation = null,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = this.getParameterWithType(stateParameter),
        sendActionParameter = this.getParameterWithType(actionParameter),
    )
}

internal fun KSFunctionDeclaration.toComposeFragmentDestinationData(
    annotation: ComposeDestination,
    resolver: Resolver,
    logger: KSPLogger,
): ComposeFragmentData? {
    val (stateParameter, actionParameter) = annotation.stateMachine.stateMachineParameters(resolver, logger)
        ?: return null

    val navigation = Navigation.Fragment(
        route = annotation.route.asClassName(),
        parentScopeIsRoute = annotation.parentScope.extendsBaseRoute(resolver),
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope.asClassName(),
    )

    return ComposeFragmentData(
        baseName = simpleName.asString(),
        packageName = packageName.asString(),
        scope = annotation.route.asClassName(),
        parentScope = annotation.parentScope.asClassName(),
        stateMachine = annotation.stateMachine.asClassName(),
        fragmentBaseClass = annotation.fragmentBaseClass.asClassName(),
        navigation = navigation,
        composableParameter = getInjectedParameters(stateParameter, actionParameter),
        stateParameter = this.getParameterWithType(stateParameter),
        sendActionParameter = this.getParameterWithType(actionParameter),
    )
}
