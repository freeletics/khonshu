package com.freeletics.khonshu.navigation.androidx

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination as AndroidXNavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost as AndroidXNavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.get
import com.freeletics.khonshu.navigation.ActivityDestination
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.LocalNavigationExecutor
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavEventNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationSetup
import com.freeletics.khonshu.navigation.OverlayDestination
import com.freeletics.khonshu.navigation.ScreenDestination
import com.freeletics.khonshu.navigation.androidx.internal.AndroidXNavigationExecutor
import com.freeletics.khonshu.navigation.androidx.internal.CustomActivityNavigator
import com.freeletics.khonshu.navigation.androidx.internal.OverlayHost
import com.freeletics.khonshu.navigation.androidx.internal.OverlayNavigator
import com.freeletics.khonshu.navigation.androidx.internal.destinationId
import com.freeletics.khonshu.navigation.androidx.internal.getArguments
import com.freeletics.khonshu.navigation.androidx.internal.handleDeepLink
import com.freeletics.khonshu.navigation.androidx.internal.requireRoute
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.findActivity
import java.io.Serializable
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

/**
 * Create a new [androidx.navigation.compose.NavHost] with a [androidx.navigation.NavGraph]
 * containing all given [destinations]. [startRoute] will be used as the start destination
 * of the graph.
 *
 * To support deep links a set of [deepLinkHandlers] can be passed in optionally.
 * These will be used to build the correct back stack when the current `Activity` was launched with
 * an `ACTION_VIEW` `Intent` that contains an url in it's data. [deepLinkPrefixes] can be used to
 * provide a default set of url patterns that should be matched by any [DeepLinkHandler] that
 * doesn't provide its own [DeepLinkHandler.prefixes].
 *
 * If a [NavEventNavigator] is passed it will be automatically set up and can be used to
 * navigate within the `NavHost`.
 *
 * The [destinationChangedCallback] can be used to be notified when the current destination
 * changes. Note that this will not be invoked when navigating to a [ActivityDestination].
 *
 * Optional [transitionAnimations] override default set of transition animations.
 */
@Composable
public fun NavHost(
    startRoute: NavRoot,
    destinations: ImmutableSet<NavDestination>,
    modifier: Modifier = Modifier,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler> = persistentSetOf(),
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix> = persistentSetOf(),
    navEventNavigator: NavEventNavigator? = null,
    destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
    transitionAnimations: NavHostTransitionAnimations = NavHostDefaults.transitionAnimations(),
) {
    val context = LocalContext.current

    // should be called before rememberNavController to fill intent data with deeplinks
    context.findActivity().handleDeepLink(deepLinkHandlers, deepLinkPrefixes)

    val overlayNavigator = remember { OverlayNavigator() }
    val customActivityNavigator = remember(context) { CustomActivityNavigator(context) }
    val navController: NavHostController = rememberNavController(overlayNavigator, customActivityNavigator)

    val composeView = LocalView.current
    DisposableEffect(navController, composeView) {
        Navigation.setViewNavController(composeView, navController)

        onDispose {
            Navigation.setViewNavController(composeView, null)
        }
    }

    // This state is used to save the start route, so that we can update the start destination of the graph.
    // It is updated when NavEventNavigation#replaceAll is called or when the `startRoute` parameter changes.
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

    SetStartDestinationSideEffect(savedStartRouteState, graph)

    CompositionLocalProvider(LocalNavigationExecutor provides executor) {
        if (navEventNavigator != null) {
            NavigationSetup(navigator = navEventNavigator)
        }

        AndroidXNavHost(
            navController = navController,
            graph = graph,
            modifier = modifier,
            enterTransition = transitionAnimations.enterTransition,
            exitTransition = transitionAnimations.exitTransition,
            popEnterTransition = transitionAnimations.popEnterTransition,
            popExitTransition = transitionAnimations.popExitTransition,
        )

        OverlayHost(
            overlayNavigator = navController.navigatorProvider[OverlayNavigator::class],
        )
    }
}

/*
 * When the start route changes because NavEventNavigation#replaceAll was called,
 * we need to update the start destination of the graph.
 *
 * This is really necessary, because after a configuration changes or a process death,
 * the NavController restores the its state, but the created graph has the wrong start destination,
 * since it was created with the `startRoute` parameter that was passed to this NavHost composable.
*/
@NonRestartableComposable
@Composable
private fun SetStartDestinationSideEffect(
    savedStartRouteState: State<BaseRoute>,
    graph: NavGraph,
) =
    LaunchedEffect(savedStartRouteState, graph) {
        snapshotFlow { savedStartRouteState.value }.collect {
            // Call graph.setStartDestination(rootId) to make sure that
            // other methods of AndroidXNavigationExecutor can access the correct start destination
            // (via controller.graph.startDestinationId).
            graph.setStartDestination(it.destinationId())
        }
    }

/**
 * Defaults used in [NavHost]
 */
public object NavHostDefaults {

    @Stable
    public fun transitionAnimations(
        enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
            { fadeIn(animationSpec = tween(700)) },
        exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
            { fadeOut(animationSpec = tween(700)) },
        popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
            enterTransition,
        popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
            exitTransition,
    ): NavHostTransitionAnimations = NavHostTransitionAnimations(
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    )
}

/**
 * Represents set of callbacks to define transition animations for destinations in [NavHost]
 */
@Stable
public class NavHostTransitionAnimations internal constructor(
    public val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition),
    public val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition),
    public val popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition),
    public val popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition),
) {
    public companion object {
        /**
         * Disables all transition animations
         */
        @Stable
        public fun noAnimations(): NavHostTransitionAnimations = NavHostTransitionAnimations(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is NavHostTransitionAnimations) return false

        if (enterTransition != other.enterTransition) return false
        if (exitTransition != other.exitTransition) return false
        if (popEnterTransition != other.popEnterTransition) return false
        if (popExitTransition != other.popExitTransition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enterTransition.hashCode()
        result = 31 * result + exitTransition.hashCode()
        result = 31 * result + popEnterTransition.hashCode()
        result = 31 * result + popExitTransition.hashCode()
        return result
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
