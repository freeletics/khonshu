package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.util.composeFragmentDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.composeFragmentFqName
import com.freeletics.mad.whetstone.codegen.util.rendererFragmentDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.rendererFragmentFqName
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference

@OptIn(ExperimentalAnvilApi::class)
internal fun ClassReference.toRendererFragmentData(): RendererFragmentData? {
    val annotation = findAnnotation(rendererFragmentFqName) ?: return null

    return RendererFragmentData(
        baseName = shortName,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass(3),
        factory = annotation.requireClassArgument("rendererFactory", 4),
        navigation = null,
        navEntryData = null,
    )
}

@OptIn(ExperimentalAnvilApi::class)
internal fun ClassReference.toRendererFragmentDestinationData(): RendererFragmentData? {
    val annotation = findAnnotation(rendererFragmentDestinationFqName) ?: return null

    val navigation = Navigation.Fragment(
        route = annotation.route,
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
        factory = annotation.requireClassArgument("rendererFactory", 6),
        navigation = navigation,
        navEntryData = navEntryData(navigation),
    )
}

@OptIn(ExperimentalAnvilApi::class)
internal fun TopLevelFunctionReference.toComposeFragmentData(): ComposeFragmentData? {
    val annotation = findAnnotation(composeFragmentFqName) ?: return null

    return ComposeFragmentData(
        baseName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass(3),
        navigation = null,
        navEntryData = null,
        composableParameter = composeParameters
    )
}

@OptIn(ExperimentalAnvilApi::class)
internal fun TopLevelFunctionReference.toComposeFragmentDestinationData(): ComposeFragmentData? {
    val annotation = findAnnotation(composeFragmentDestinationFqName) ?: return null

    val navigation = Navigation.Fragment(
        route = annotation.route,
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope,
    )

    return ComposeFragmentData(
        baseName = name,
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.fragmentBaseClass(5),
        navigation = navigation,
        navEntryData = navEntryData(navigation),
        composableParameter = composeParameters
    )
}
