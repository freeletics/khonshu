package com.freeletics.khonshu.codegen.parser.anvil

import com.freeletics.khonshu.codegen.ComposeFragmentData
import com.freeletics.khonshu.codegen.Navigation
import com.freeletics.khonshu.codegen.RendererFragmentData
import com.freeletics.khonshu.codegen.codegen.util.composeFragmentDestinationFqName
import com.freeletics.khonshu.codegen.codegen.util.composeFragmentFqName
import com.freeletics.khonshu.codegen.codegen.util.rendererFragmentDestinationFqName
import com.freeletics.khonshu.codegen.codegen.util.rendererFragmentFqName
import com.freeletics.khonshu.codegen.codegen.util.stateMachineFqName
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference
import com.squareup.anvil.compiler.internal.reference.asClassName

internal fun ClassReference.toRendererFragmentData(): RendererFragmentData? {
    val annotation = findAnnotation(rendererFragmentFqName) ?: return null

    return RendererFragmentData(
        baseName = shortName,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass(3),
        factory = findRendererFactory(),
        navigation = null,
    )
}

internal fun ClassReference.toRendererFragmentDestinationData(): RendererFragmentData? {
    val annotation = findAnnotation(rendererFragmentDestinationFqName) ?: return null

    val navigation = Navigation.Fragment(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScopeReference.extendsBaseRoute(),
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope,
    )

    return RendererFragmentData(
        baseName = shortName,
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass(5),
        factory = findRendererFactory(),
        navigation = navigation,
    )
}

internal fun TopLevelFunctionReference.toComposeFragmentData(): ComposeFragmentData? {
    val annotation = findAnnotation(composeFragmentFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val stateMachineSuperType = stateMachine.superTypeReference(stateMachineFqName)
    val stateParameter = stateMachine.stateMachineStateParameter(stateMachineSuperType)
    val actionParameter = stateMachine.stateMachineActionFunctionParameter(stateMachineSuperType)

    return ComposeFragmentData(
        baseName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        fragmentBaseClass = annotation.fragmentBaseClass(3),
        navigation = null,
        composableParameter = getComposeParameters(stateParameter, actionParameter),
        stateParameter = getStateParameter(stateParameter),
        sendActionParameter = getSendActionParameter(actionParameter),
    )
}

internal fun TopLevelFunctionReference.toComposeFragmentDestinationData(): ComposeFragmentData? {
    val annotation = findAnnotation(composeFragmentDestinationFqName) ?: return null

    val stateMachine = annotation.stateMachineReference
    val stateMachineSuperType = stateMachine.superTypeReference(stateMachineFqName)
    val stateParameter = stateMachine.stateMachineStateParameter(stateMachineSuperType)
    val actionParameter = stateMachine.stateMachineActionFunctionParameter(stateMachineSuperType)

    val navigation = Navigation.Fragment(
        route = annotation.route,
        parentScopeIsRoute = annotation.parentScopeReference.extendsBaseRoute(),
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope,
    )

    return ComposeFragmentData(
        baseName = name,
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = stateMachine.asClassName(),
        fragmentBaseClass = annotation.fragmentBaseClass(5),
        navigation = navigation,
        composableParameter = getComposeParameters(stateParameter, actionParameter),
        stateParameter = getStateParameter(stateParameter),
        sendActionParameter = getSendActionParameter(actionParameter),
    )
}
