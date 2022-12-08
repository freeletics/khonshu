package com.freeletics.mad.navigator.fragment

import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.internal.CustomActivityNavigator
import com.freeletics.mad.navigator.internal.activityDestinationId
import com.freeletics.mad.navigator.internal.destinationId
import com.freeletics.mad.navigator.internal.getArguments

/**
 * Creates and sets a [androidx.navigation.NavGraph] containing all given [destinations].
 * [startRoute] will be used as the start destination of the graph.
 */
public fun NavHostFragment.setGraph(
    startRoute: BaseRoute,
    destinations: Set<NavDestination>,
) {
    navController.navigatorProvider.addNavigator(CustomActivityNavigator(requireContext()))
    @Suppress("deprecation")
    val graph = navController.createGraph(startDestination = startRoute.destinationId()) {
        destinations.forEach { destination ->
            addDestination(navController, destination, startRoute)
        }
    }

    navController.setGraph(graph, null)
}

private fun NavGraphBuilder.addDestination(
    controller: NavController,
    destination: NavDestination,
    startRoute: BaseRoute,
) {
    val newDestination = when (destination) {
        is NavDestination.Screen<*> -> destination.toDestination(controller, startRoute)
        is NavDestination.Dialog<*> -> destination.toDestination(controller)
        is NavDestination.Activity<*> -> destination.toDestination(controller)
    }
    addDestination(newDestination)
}

private fun NavDestination.Screen<*>.toDestination(
    controller: NavController,
    startRoute: BaseRoute,
): FragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[FragmentNavigator::class]
    return FragmentNavigator.Destination(navigator).also {
        it.id = route.destinationId()
        it.setClassName(fragmentClass.java.name)
        if (startRoute::class == route) {
            val arguments = startRoute.getArguments()
            arguments.keySet().forEach { key ->
                val argument = NavArgument.Builder()
                    .setDefaultValue(arguments.get(key))
                    .setIsNullable(false)
                    .build()
                it.addArgument(key, argument)
            }
        }
    }
}

private fun NavDestination.Dialog<*>.toDestination(
    controller: NavController,
): DialogFragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[DialogFragmentNavigator::class]
    return DialogFragmentNavigator.Destination(navigator).also {
        it.id = route.destinationId()
        it.setClassName(fragmentClass.java.name)
    }
}

private fun NavDestination.Activity<*>.toDestination(
    controller: NavController,
): CustomActivityNavigator.Destination {
    val navigator = controller.navigatorProvider[CustomActivityNavigator::class]
    return CustomActivityNavigator.Destination(navigator).also {
        it.id = route.activityDestinationId()
        it.intent = intent
    }
}
