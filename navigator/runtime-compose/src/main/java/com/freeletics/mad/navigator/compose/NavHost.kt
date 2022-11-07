package com.freeletics.mad.navigator.compose

import androidx.navigation.compose.NavHost as AndroidXNavHost
import android.os.Bundle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
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
import com.freeletics.mad.navigator.internal.CustomActivityNavigator
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.activityDestinationId
import com.freeletics.mad.navigator.internal.destinationId
import com.freeletics.mad.navigator.internal.getArguments
import com.freeletics.mad.navigator.internal.requireRoute
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator

/**
 * Create a new [androidx.navigation.compose.NavHost] with a [androidx.navigation.NavGraph]
 * containing all given [destinations]. [startRoute] will be used as the start destination
 * of the graph.
 *
 * The [destinationChangedCallback] can be used to be notified when the current destination
 * changes. Note that this will not be invoked when navigating to a [NavDestination.Activity].
 */
@ExperimentalMaterialNavigationApi
@Composable
public fun NavHost(
    startRoute: BaseRoute,
    destinations: Set<NavDestination>,
    destinationChangedCallback: ((NavRoute) -> Unit)? = null,
    bottomSheetShape: Shape = MaterialTheme.shapes.large,
    bottomSheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    bottomSheetBackgroundColor: Color = MaterialTheme.colors.surface,
    bottomSheetContentColor: Color = contentColorFor(bottomSheetBackgroundColor),
    bottomSheetScrimColor: Color = ModalBottomSheetDefaults.scrimColor,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    val context = LocalContext.current

    if (destinationChangedCallback != null) {
        DisposableEffect(key1 = destinationChangedCallback) {
            val listener = OnDestinationChangedListener { _, _, arguments ->
                val route = arguments.requireRoute<NavRoute>()
                destinationChangedCallback.invoke(route)
            }
            navController.addOnDestinationChangedListener(listener)

            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }
    }

    val graph = remember(navController, context, startRoute, destinations) {
        navController.navigatorProvider.addNavigator(CustomActivityNavigator(context))
        @Suppress("deprecation")
        navController.createGraph(startDestination = startRoute.destinationId()) {
            destinations.forEach { destination ->
                addDestination(navController, destination, startRoute)
            }
        }
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        ModalBottomSheetLayout(
            bottomSheetNavigator = bottomSheetNavigator,
            sheetShape = bottomSheetShape,
            sheetElevation = bottomSheetElevation,
            sheetBackgroundColor = bottomSheetBackgroundColor,
            sheetContentColor = bottomSheetContentColor,
            scrimColor = bottomSheetScrimColor,
        ) {
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
    return ComposeNavigator.Destination(navigator) { screenContent(it.arguments.requireRoute()) }.also {
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
    return DialogNavigator.Destination(navigator) { dialogContent(it.arguments.requireRoute()) }.also {
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
    return BottomSheetNavigator.Destination(navigator) { bottomSheetContent(it.arguments.requireRoute()) }.also {
        it.id = route.destinationId()
    }
}

private fun Activity.toDestination(
    controller: NavController,
): CustomActivityNavigator.Destination {
    val navigator = controller.navigatorProvider[CustomActivityNavigator::class]
    return CustomActivityNavigator.Destination(navigator).also {
        it.id = route.activityDestinationId()
        it.intent = intent
    }
}

@InternalNavigatorApi
public val LocalNavController: ProvidableCompositionLocal<NavController> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
