package com.freeletics.mad.navigator.fragment

import androidx.navigation.NavDestination as AndroidXNavDestination
import android.os.Bundle
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute

/**
 * Creates and sets a [androidx.navigation.NavGraph] containing all given [destinations].
 * [startRoot] will be used as the start destination of the graph.
 *
 * [destinationCreator] can be passed to add support for custom subclasses of [NavDestination].
 */
public fun NavHostFragment.setGraph(
    startRoot: NavRoot,
    destinations: Set<NavDestination>,
    destinationCreator: (NavDestination) -> AndroidXNavDestination? = { null },
) {
    val startDestinationId = startRoot.destinationId
    val startDestinationArgs = startRoot.getArguments()
    navController.setGraph(startDestinationId, startDestinationArgs, destinations, destinationCreator)
}

/**
 * Creates and sets a [androidx.navigation.NavGraph] containing all given [destinations].
 * [startRoute] will be used as the start destination of the graph.
 *
 * [destinationCreator] can be passed to add support for custom subclasses of [NavDestination].
 */
public fun NavHostFragment.setGraph(
    startRoute: NavRoute,
    destinations: Set<NavDestination>,
    destinationCreator: (NavDestination) -> AndroidXNavDestination? = { null },
) {
    val startDestinationId = startRoute.destinationId
    val startDestinationArgs = startRoute.getArguments()
    navController.setGraph(startDestinationId, startDestinationArgs, destinations, destinationCreator)
}


private fun NavController.setGraph(
    startDestinationId: Int,
    startDestinationArgs: Bundle,
    destinations: Set<NavDestination>,
    destinationCreator: (NavDestination) -> AndroidXNavDestination?,
) {
    @Suppress("deprecation")
    val graph = createGraph(startDestination = startDestinationId) {
        destinations.forEach { destination ->
            addDestination(this@setGraph, destination, destinationCreator)
        }
    }
    setGraph(graph, startDestinationArgs)
}

private fun NavGraphBuilder.addDestination(
    controller: NavController,
    destination: NavDestination,
    destinationCreator: (NavDestination) -> AndroidXNavDestination?,
) {
    val newDestination = when (destination) {
        is NavDestination.Screen -> destination.toDestination(controller)
        is NavDestination.RootScreen -> destination.toDestination(controller)
        is NavDestination.Dialog -> destination.toDestination(controller)
        is NavDestination.Activity -> destination.toDestination(controller)
        else -> destinationCreator(destination)
    } ?: throw IllegalArgumentException("Unable to create destination for unknown type " +
        "${destination::class.java}. Handle it in destinationCreator")

    addDestination(newDestination)
}

private fun NavDestination.Screen.toDestination(
    controller: NavController,
): FragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[FragmentNavigator::class]
    return FragmentNavigator.Destination(navigator).also {
        it.id = destinationId
        it.setClassName(fragmentClass.java.name)
    }
}

private fun NavDestination.RootScreen.toDestination(
    controller: NavController,
): FragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[FragmentNavigator::class]
    return FragmentNavigator.Destination(navigator).also {
        it.id = destinationId
        it.setClassName(fragmentClass.java.name)
    }
}

private fun NavDestination.Dialog.toDestination(
    controller: NavController,
): DialogFragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[DialogFragmentNavigator::class]
    return DialogFragmentNavigator.Destination(navigator).also {
        it.id = destinationId
        it.setClassName(fragmentClass.java.name)
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
