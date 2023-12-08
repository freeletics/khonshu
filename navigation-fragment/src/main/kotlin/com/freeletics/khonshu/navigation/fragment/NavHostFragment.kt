package com.freeletics.khonshu.navigation.fragment

import androidx.navigation.NavDestination as AndroidXNavDestination
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.CustomActivityNavigator
import com.freeletics.khonshu.navigation.internal.destinationId
import com.freeletics.khonshu.navigation.internal.getArguments
import com.freeletics.khonshu.navigation.internal.handleDeepLink
import java.io.Serializable
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Creates and sets a [androidx.navigation.NavGraph] containing all given [destinations].
 * [startRoute] will be used as the start destination of the graph.
 *
 * To support deep links a set of [DeepLinkHandlers][DeepLinkHandler] can be passed in optionally.
 * These will be used to build the correct back stack when the current `Activity` was launched with
 * an `ACTION_VIEW` `Intent` that contains an url in it's `data. [deepLinkPrefixes] can be used to
 * provide a default set of url patterns that should be matched by any [DeepLinkHandler] that
 * doesn't provide its own [DeepLinkHandler.prefixes].
 */
@Deprecated("Fragment support will be removed in the next release")
@Suppress("DEPRECATION")
public fun NavHostFragment.setGraph(
    startRoute: NavRoot,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
) {
    requireActivity().handleDeepLink(deepLinkHandlers, deepLinkPrefixes)

    navController.navigatorProvider.addNavigator(CustomActivityNavigator(requireContext()))
    @Suppress("deprecation")
    val graph = navController.createGraph(startDestination = startRoute.destinationId()) {
        destinations.forEach { destination ->
            addDestination(navController, destination, startRoute)
        }
    }

    navController.setGraph(graph, null)

    ViewModelProvider(this)[HandleNavigationViewModel::class.java].run {
        if (savedStartRouteState.value == null) {
            onSaveStartRoute(startRoute)
        }
        savedStartRouteState
            .filterNotNull()
            .onEach {
                println("$this setGraph $it")
                graph.setStartDestination(it.destinationId())
            }
            .launchIn(lifecycleScope)
    }
}

@Suppress("DEPRECATION")
private fun NavGraphBuilder.addDestination(
    controller: NavController,
    destination: NavDestination,
    startRoute: BaseRoute,
) {
    val newDestination = when (destination) {
        is ScreenDestination<*> -> destination.toDestination(controller, startRoute)
        is DialogDestination<*> -> destination.toDestination(controller)
        is ActivityDestination<*> -> destination.toDestination(controller)
    }
    addDestination(newDestination)
}

private fun ScreenDestination<*>.toDestination(
    controller: NavController,
    startRoute: BaseRoute,
): FragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[FragmentNavigator::class]
    return FragmentNavigator.Destination(navigator).also {
        it.id = id.destinationId()
        it.setClassName(fragmentClass)
        it.addExtra(extra)
        if (startRoute::class == id.route) {
            val arguments = startRoute.getArguments()
            arguments.keySet().forEach { key ->
                @Suppress("DEPRECATION")
                val argument = NavArgument.Builder()
                    .setDefaultValue(arguments.get(key))
                    .setIsNullable(false)
                    .build()
                it.addArgument(key, argument)
            }
        }
    }
}

private fun DialogDestination<*>.toDestination(
    controller: NavController,
): DialogFragmentNavigator.Destination {
    val navigator = controller.navigatorProvider[DialogFragmentNavigator::class]
    return DialogFragmentNavigator.Destination(navigator).also {
        it.id = id.destinationId()
        it.setClassName(fragmentClass)
        it.addExtra(extra)
    }
}

private fun AndroidXNavDestination.addExtra(extra: Serializable?) {
    if (extra == null) {
        return
    }

    val argument = NavArgument.Builder()
        .setDefaultValue(extra)
        .setIsNullable(false)
        .build()
    addArgument("NAV_SECRET_EXTRA", argument)
}

private fun ActivityDestination<*>.toDestination(
    controller: NavController,
): CustomActivityNavigator.Destination {
    val navigator = controller.navigatorProvider[CustomActivityNavigator::class]
    return CustomActivityNavigator.Destination(navigator).also {
        it.id = id.destinationId()
        it.intent = intent
    }
}
