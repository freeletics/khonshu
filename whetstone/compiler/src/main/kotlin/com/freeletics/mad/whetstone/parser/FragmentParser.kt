package com.freeletics.mad.whetstone.parser

import com.freeletics.mad.whetstone.ComposeFragmentData
import com.freeletics.mad.whetstone.Navigation
import com.freeletics.mad.whetstone.RendererFragmentData
import com.freeletics.mad.whetstone.codegen.util.composeFragmentFqName
import com.freeletics.mad.whetstone.codegen.util.fragment
import com.freeletics.mad.whetstone.codegen.util.fragmentNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.fragmentRootNavDestinationFqName
import com.freeletics.mad.whetstone.codegen.util.rendererFragmentFqName
import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.squareup.anvil.compiler.internal.reference.AnnotatedReference
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.TopLevelFunctionReference

@OptIn(ExperimentalAnvilApi::class)
internal fun ClassReference.toRendererFragmentData(): RendererFragmentData? {
    val annotation = findAnnotation(rendererFragmentFqName) ?: return null
    val navigation = fragmentNavigation()

    return RendererFragmentData(
        baseName = shortName,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        factory = annotation.requireClassArgument("rendererFactory", 3),
        fragmentBaseClass = annotation.optionalClassArgument("fragmentBaseClass", 4) ?: fragment,
        navigation = navigation,
        navEntryData = navEntryData(navigation)
    )
}

@OptIn(ExperimentalAnvilApi::class)
internal fun TopLevelFunctionReference.toComposeFragmentData(): ComposeFragmentData? {
    val annotation = findAnnotation(composeFragmentFqName) ?: return null
    val navigation = fragmentNavigation()

    return ComposeFragmentData(
        baseName = name,
        packageName = packageName,
        scope = annotation.scope,
        parentScope = annotation.parentScope,
        stateMachine = annotation.stateMachine,
        fragmentBaseClass = annotation.optionalClassArgument("fragmentBaseClass", 3) ?: fragment,
        navigation = navigation,
        navEntryData = navEntryData(navigation),
        composableParameter = composeParameters
    )
}

@OptIn(ExperimentalAnvilApi::class)
private fun AnnotatedReference.fragmentNavigation(): Navigation.Fragment? {
    val navigation = findAnnotation(fragmentNavDestinationFqName)
    if (navigation != null) {
        val route = navigation.requireClassArgument("route", 0)
        val destinationScope = navigation.requireClassArgument("destinationScope", 2)
        return Navigation.Fragment(
            route = route,
            destinationType = navigation.requireEnumArgument("type", 1),
            destinationScope = destinationScope,
        )
    }

    val rootNavigation = findAnnotation(fragmentRootNavDestinationFqName)
    if (rootNavigation != null) {
        val route = rootNavigation.requireClassArgument("root", 0)
        val destinationScope = rootNavigation.requireClassArgument("destinationScope", 1)
        return Navigation.Fragment(
            route = route,
            destinationType = "SCREEN",
            destinationScope = destinationScope,
        )
    }

    return null
}
