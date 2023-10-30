package com.freeletics.khonshu.navigation.compose

import androidx.navigation.NavDestination as AndroidXNavDestination
import androidx.navigation.compose.NavHost as AndroidXNavHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.get
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.compose.internal.OverlayHost
import com.freeletics.khonshu.navigation.compose.internal.OverlayNavigator
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.AndroidXNavigationExecutor
import com.freeletics.khonshu.navigation.internal.CustomActivityNavigator
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import com.freeletics.khonshu.navigation.internal.destinationId
import com.freeletics.khonshu.navigation.internal.getArguments
import com.freeletics.khonshu.navigation.internal.handleDeepLink
import com.freeletics.khonshu.navigation.internal.requireRoute
import java.io.Serializable

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
@Composable
public fun NavHost(
    startRoute: NavRoot,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
    destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
) {
    val context = LocalContext.current

    val overlayNavigator = remember { OverlayNavigator() }
    val customActivityNavigator = remember(context) { CustomActivityNavigator(context) }
    val navController = rememberNavController(overlayNavigator, customActivityNavigator)

    // This state is used to save the start route, so that we can update the start destination of the graph.
    // It is updated when NavEventNavigation#replaceRoot is called or when the `startRoute` parameter changes.
    val savedStartRouteState = rememberSaveable { mutableStateOf(startRoute) }

    val executor = remember(navController, savedStartRouteState) {
        AndroidXNavigationExecutor(
            controller = navController,
            onSaveStartRoute = { savedStartRouteState.value = it },
        )
    }

    if (destinationChangedCallback != null) {
        DisposableEffect(navController, destinationChangedCallback) {
            val listener = OnDestinationChangedListener { _, _, arguments ->
                val route = arguments.requireRoute<BaseRoute>()
                destinationChangedCallback.invoke(route)
            }
            navController.addOnDestinationChangedListener(listener)

            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }
    }

    DisposableEffect(context, deepLinkHandlers, deepLinkPrefixes) {
        context.findActivity().handleDeepLink(deepLinkHandlers, deepLinkPrefixes)
        onDispose { }
    }

    val graph = remember(navController, startRoute, destinations) {
        @Suppress("deprecation")
        navController.createGraph(startDestination = startRoute.destinationId()) {
            destinations.forEach { destination ->
                addDestination(navController, destination, startRoute)
            }
        }.also {
            // Update the saved start route when a new graph is created.
            savedStartRouteState.value = startRoute
        }
    }

    // When the start route changes because NavEventNavigation#replaceRoot was called,
    // we need to update the start destination of the graph.
    //
    // This is really necessary, because after a configuration changes or a process death,
    // the NavController restores the its state, but the created graph has the wrong start destination,
    // since it was created with the `startRoute` parameter that was passed to this NavHost composable.
    LaunchedEffect(savedStartRouteState, graph) {
        snapshotFlow { savedStartRouteState.value }.collect {
            // Call graph.setStartDestination(rootId) to make sure that
            // other methods of AndroidXNavigationExecutor can access the correct start destination
            // (via controller.graph.startDestinationId).
            graph.setStartDestination(it.destinationId())
        }
    }

    CompositionLocalProvider(LocalNavigationExecutor provides executor) {
        AndroidXNavHost(
            navController = navController,
            graph = graph,
        )

        OverlayHost(
            overlayNavigator = overlayNavigator,
        )
    }
}

// the BottomSheet class and creator methods are marked with ExperimentalMaterialNavigationApi
// if those stay unused the experimental code is never called, so we swallow the warning here
private fun NavGraphBuilder.addDestination(
    controller: NavController,
    destination: NavDestination,
    startRoute: BaseRoute,
) {
    val newDestination = when (destination) {
        is ScreenDestination<*> -> destination.toDestination(controller, startRoute)
        is OverlayDestination<*> -> destination.toDestination(controller)
        is ActivityDestination -> destination.toDestination(controller)
    }
    addDestination(newDestination)
}

private fun <T : BaseRoute> ScreenDestination<T>.toDestination(
    controller: NavController,
    startRoute: BaseRoute,
): ComposeNavigator.Destination {
    val navigator = controller.navigatorProvider[ComposeNavigator::class]
    return ComposeNavigator.Destination(navigator) { content(it.arguments.requireRoute()) }.also {
        it.id = id.destinationId()
        it.addExtra(extra)
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

private fun <T : NavRoute> OverlayDestination<T>.toDestination(
    controller: NavController,
): OverlayNavigator.Destination {
    val navigator = controller.navigatorProvider[OverlayNavigator::class]
    return OverlayNavigator.Destination(navigator) { content(it.arguments.requireRoute()) }.also {
        it.id = id.destinationId()
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

private fun ActivityDestination.toDestination(
    controller: NavController,
): CustomActivityNavigator.Destination {
    val navigator = controller.navigatorProvider[CustomActivityNavigator::class]
    return CustomActivityNavigator.Destination(navigator).also {
        it.id = id.destinationId()
        it.intent = intent
    }
}

@InternalNavigationApi
public val LocalNavigationExecutor: ProvidableCompositionLocal<NavigationExecutor> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
