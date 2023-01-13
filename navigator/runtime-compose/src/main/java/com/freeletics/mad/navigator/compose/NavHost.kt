package com.freeletics.mad.navigator.compose

import androidx.navigation.compose.NavHost as AndroidXNavHost
import android.content.Intent
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

import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.internal.AndroidXNavigationExecutor
import com.freeletics.mad.navigator.internal.CustomActivityNavigator
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.NavigationExecutor
import com.freeletics.mad.navigator.internal.destinationId
import com.freeletics.mad.navigator.internal.getArguments
import com.freeletics.mad.navigator.internal.handleDeepLink
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
 * To support deep links a set of [DeepLinkHandlers][DeepLinkHandler] can be passed in optionally.
 * These will be used to build the correct back stack when the current `Activity` was launched with
 * an `ACTION_VIEW` `Intent` that contains an url in it's data. [deepLinkPrefixes] can be used to
 * provide a default set of url patterns that should be matched by any [DeepLinkHandler] that
 * doesn't provide its own [DeepLinkHandler.prefixes].
 *
 * The [destinationChangedCallback] can be used to be notified when the current destination
 * changes. Note that this will not be invoked when navigating to a [ActivityDestination].
 */
@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
public fun NavHost(
    startRoute: BaseRoute,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
    destinationChangedCallback: ((NavRoute) -> Unit)? = null,
    bottomSheetShape: Shape = MaterialTheme.shapes.large,
    bottomSheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    bottomSheetBackgroundColor: Color = MaterialTheme.colors.surface,
    bottomSheetContentColor: Color = contentColorFor(bottomSheetBackgroundColor),
    bottomSheetScrimColor: Color = ModalBottomSheetDefaults.scrimColor,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    val executor = remember(navController) { AndroidXNavigationExecutor(navController) }
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
        context.findActivity().handleDeepLink(deepLinkHandlers, deepLinkPrefixes)

        navController.navigatorProvider.addNavigator(CustomActivityNavigator(context))
        @Suppress("deprecation")
        navController.createGraph(startDestination = startRoute.destinationId()) {
            destinations.forEach { destination ->
                addDestination(navController, destination, startRoute)
            }
        }
    }

    CompositionLocalProvider(LocalNavigationExecutor provides executor) {
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
        is ScreenDestination<*> -> destination.toDestination(controller, startRoute)
        is DialogDestination<*> -> destination.toDestination(controller)
        is BottomSheetDestination<*> -> destination.toDestination(controller)
        is ActivityDestination -> destination.toDestination(controller)
    }
    addDestination(newDestination)
}

private fun <T : BaseRoute> ScreenDestination<T>.toDestination(
    controller: NavController,
    startRoute: BaseRoute,
): ComposeNavigator.Destination {
    val navigator = controller.navigatorProvider[ComposeNavigator::class]
    return ComposeNavigator.Destination(navigator) { screenContent(it.arguments.requireRoute()) }.also {
        it.id = id.destinationId()
        if (startRoute::class == id.route) {
            val arguments = startRoute.getArguments()
            @Suppress("DEPRECATION")
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

private fun <T : NavRoute> DialogDestination<T>.toDestination(
    controller: NavController,
): DialogNavigator.Destination {
    val navigator = controller.navigatorProvider[DialogNavigator::class]
    return DialogNavigator.Destination(navigator) { dialogContent(it.arguments.requireRoute()) }.also {
        it.id = id.destinationId()
    }
}

// the BottomSheet class and creator methods are marked with ExperimentalMaterialNavigationApi
// if those stay unused this method is never called, so we swallow the warning here
@OptIn(ExperimentalMaterialNavigationApi::class)
private fun <T : NavRoute> BottomSheetDestination<T>.toDestination(
    controller: NavController,
): BottomSheetNavigator.Destination {
    val navigator = controller.navigatorProvider[BottomSheetNavigator::class]
    return BottomSheetNavigator.Destination(navigator) { bottomSheetContent(it.arguments.requireRoute()) }.also {
        it.id = id.destinationId()
    }
}

private fun ActivityDestination.toDestination(
    controller: NavController,
): CustomActivityNavigator.Destination {
    val navigator = controller.navigatorProvider[CustomActivityNavigator::class]
    return CustomActivityNavigator.Destination(navigator).also {
        it.id = id.destinationId()
        it.intent = intent
    }
}

@InternalNavigatorApi
public val LocalNavigationExecutor: ProvidableCompositionLocal<NavigationExecutor> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
