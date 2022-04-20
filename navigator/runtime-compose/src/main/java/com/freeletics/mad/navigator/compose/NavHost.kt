package com.freeletics.mad.navigator.compose

import androidx.navigation.compose.NavHost as AndroidXNavHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.get
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.compose.NavDestination.Activity
import com.freeletics.mad.navigator.compose.NavDestination.BottomSheet
import com.freeletics.mad.navigator.compose.NavDestination.Dialog
import com.freeletics.mad.navigator.compose.NavDestination.Screen
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.activityDestinationId
import com.freeletics.mad.navigator.internal.destinationId
import com.freeletics.mad.navigator.internal.getArguments
import com.freeletics.mad.navigator.internal.toRoute
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator

/**
 * Create a new [androidx.navigation.compose.NavHost] with a [androidx.navigation.NavGraph]
 * containing all given [destinations]. [startRoute] will be used as the start destination
 * of the graph.
 */
@ExperimentalMaterialNavigationApi
@Composable
public fun NavHost(
    startRoute: BaseRoute,
    destinations: Set<NavDestination>,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    val graph = remember(navController, startRoute, destinations) {
        @Suppress("deprecation")
        navController.createGraph(startDestination = startRoute.destinationId()) {
            destinations.forEach { destination ->
                addDestination(navController, destination, startRoute)
            }
        }
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        ModalBottomSheetLayout(bottomSheetNavigator) {
            AndroidXNavHost(navController, graph)
        }
    }
}

// the BottomSheet class and creator methods are marked with ExperimentalMaterialNavigationApi
// if those stay unused the experimental code is never called, so we swallow the warning here
@OptIn(ExperimentalMaterialNavigationApi::class)
private fun NavGraphBuilder.addDestination(
    controller: NavController,
    destination: NavDestination,
    startRoute: BaseRoute,
) {
    val newDestination = when (destination) {
        is Screen<*> -> destination.toDestination(controller, startRoute)
        is Dialog<*> -> destination.toDestination(controller)
        is BottomSheet<*> -> destination.toDestination(controller)
        is Activity -> destination.toDestination(controller)
    }
    addDestination(newDestination)
}

private fun <T : BaseRoute> Screen<T>.toDestination(
    controller: NavController,
    startRoute: BaseRoute,
): ComposeNavigator.Destination {
    val navigator = controller.navigatorProvider[ComposeNavigator::class]
    return ComposeNavigator.Destination(navigator) { screenContent(it.arguments!!.toRoute()) }.also {
        it.id = route.destinationId()
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

private fun <T : NavRoute> Dialog<T>.toDestination(
    controller: NavController,
): DialogNavigator.Destination {
    val navigator = controller.navigatorProvider[DialogNavigator::class]
    return DialogNavigator.Destination(navigator) { dialogContent(it.arguments!!.toRoute()) }.also {
        it.id = route.destinationId()
    }
}

// the BottomSheet class and creator methods are marked with ExperimentalMaterialNavigationApi
// if those stay unused this method is never called, so we swallow the warning here
@OptIn(ExperimentalMaterialNavigationApi::class)
private fun <T : NavRoute> BottomSheet<T>.toDestination(
    controller: NavController,
): BottomSheetNavigator.Destination {
    val navigator = controller.navigatorProvider[BottomSheetNavigator::class]
    return BottomSheetNavigator.Destination(navigator) { bottomSheetContent(it.arguments!!.toRoute()) }.also {
        it.id = route.destinationId()
    }
}

private fun Activity.toDestination(
    controller: NavController,
): ActivityNavigator.Destination {
    val navigator = controller.navigatorProvider[ActivityNavigator::class]
    return ActivityNavigator.Destination(navigator).also {
        it.id = route.activityDestinationId()
        it.setIntent(intent)
        it.setDataPattern("{test}")
    }
}

@InternalNavigatorApi
public val LocalNavController: ProvidableCompositionLocal<NavController> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
