package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposeScreenData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.codegen.FileGenerator
import com.freeletics.mad.whetstone.codegen.util.composeFqName
import com.freeletics.mad.whetstone.codegen.util.composeNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.composeRootNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.whetstoneComposeScreenDestinationFqName
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.api.GeneratedFile
import com.squareup.anvil.compiler.api.createGeneratedFile
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
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

@OptIn(ExperimentalAnvilApi::class)
private fun AnnotatedReference.composeNavigation(): Navigation.Compose? {
    val navigation = findAnnotation(composeNavDestinationFqName)
    if (navigation != null) {
        val route = navigation.requireClassArgument("route", 0)
        val destinationScope = navigation.requireClassArgument("destinationScope", 2)
        return Navigation.Compose(
            route = route,
            destinationType = navigation.requireEnumArgument("type", 1),
            destinationScope = destinationScope,
        )
    }

    val rootNavigation = findAnnotation(composeRootNavDestinationFqName)
    if (rootNavigation != null) {
        val route = rootNavigation.requireClassArgument("root", 0)
        val destinationScope = rootNavigation.requireClassArgument("destinationScope", 1)
        return Navigation.Compose(
            route = route,
            destinationType = "SCREEN",
            destinationScope = destinationScope,
        )
    }

    return null
}
