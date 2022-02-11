package com.freeletics.mad.navigator.fragment

import androidx.navigation.NavDestination as AndroidXNavDestination
import android.os.Bundle
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.internal.getArguments

/**
 * Creates and sets a [androidx.navigation.NavGraph] containing all given [destinations].
 * [startRoot] will be used as the start destination of the graph.
 */
public fun NavHostFragment.setGraph(
    startRoot: NavRoot,
    destinations: Set<NavDestination>,
) {
    val startDestinationId = startRoot.destinationId
    val startDestinationArgs = startRoot.getArguments()
    navController.setGraph(startDestinationId, startDestinationArgs, destinations)
}

/**
 * Creates and sets a [androidx.navigation.NavGraph] containing all given [destinations].
 * [startRoute] will be used as the start destination of the graph.
 */
public fun NavHostFragment.setGraph(
    startRoute: NavRoute,
    destinations: Set<NavDestination>,
) {
    val startDestinationId = startRoute.destinationId
    val startDestinationArgs = startRoute.getArguments()
    navController.setGraph(startDestinationId, startDestinationArgs, destinations)
}


private fun NavController.setGraph(
    startDestinationId: Int,
    startDestinationArgs: Bundle,
    destinations: Set<NavDestination>,
) {
    @Suppress("deprecation")
    val graph = createGraph(startDestination = startDestinationId) {
        destinations.forEach { destination ->
            addDestination(this@setGraph, destination)
        }
    }
    setGraph(graph, startDestinationArgs)
}

private fun NavGraphBuilder.addDestination(
    controller: NavController,
    destination: NavDestination,
) {
    val newDestination = when (destination) {
        is NavDestination.Screen -> destination.toDestination(controller)
        is NavDestination.RootScreen -> destination.toDestination(controller)
        is NavDestination.Dialog -> destination.toDestination(controller)
        is NavDestination.Activity -> destination.toDestination(controller)
    }
    addDestination(newDestination)
}

private fun NavDestination.Screen.toDestination(
    controller: NavController,
): FragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[FragmentNavigator::class]
    return FragmentNavigator.Destination(navigator).also {
        it.id = destinationId
        it.setClassName(fragmentClass.java.name)
        it.addDefaultArguments(defaultArguments)
    }
}

private fun NavDestination.RootScreen.toDestination(
    controller: NavController,
): FragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[FragmentNavigator::class]
    return FragmentNavigator.Destination(navigator).also {
        it.id = destinationId
        it.setClassName(fragmentClass.java.name)
        it.addDefaultArguments(defaultArguments)
    }
}

private fun NavDestination.Dialog.toDestination(
    controller: NavController,
): DialogFragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[DialogFragmentNavigator::class]
    return DialogFragmentNavigator.Destination(navigator).also {
        it.id = destinationId
        it.setClassName(fragmentClass.java.name)
        it.addDefaultArguments(defaultArguments)
    }
}

private fun NavDestination.Activity.toDestination(
    controller: NavController,
): ActivityNavigator.Destination {
    val navigator = controller.navigatorProvider[ActivityNavigator::class]
    return ActivityNavigator.Destination(navigator).also {
        it.id = destinationId
        it.setIntent(intent)
    }
}

private fun AndroidXNavDestination.addDefaultArguments(extras: Bundle?) {
    extras?.keySet()?.forEach { key ->
        val argument = NavArgument.Builder()
            .setDefaultValue(extras.get(key))
            .setIsNullable(false)
            .build()
        addArgument(key, argument)
    }
}
