package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.codegen.util.composeFqName
import com.freeletics.mad.whetstone.codegen.util.whetstoneComposeScreenDestinationFqName
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference

@OptIn(ExperimentalAnvilApi::class)
internal fun TopLevelFunctionReference.toComposeScreenData(): ComposeScreenData? {
    val annotation = findAnnotation(composeFqName) ?: return null

    return ComposeScreenData(
        baseName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        navigation = null,
        navEntryData = null,
        composableParameter = composeParameters
    )
}

@OptIn(ExperimentalAnvilApi::class)
internal fun TopLevelFunctionReference.toComposeScreenDestinationData(): ComposeScreenData? {
    val annotation = findAnnotation(whetstoneComposeScreenDestinationFqName) ?: return null

    val navigation = Navigation.Compose(
        route = annotation.route,
        destinationType = annotation.destinationType,
        destinationScope = annotation.destinationScope,
    )

    return ComposeScreenData(
        baseName = name,
        packageName = packageName,
        scope = annotation.route,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        navigation = navigation,
        navEntryData = navEntryData(navigation),
        composableParameter = composeParameters
    )
}
