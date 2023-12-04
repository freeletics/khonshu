package com.freeletics.khonshu.navigation.compose

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination as AndroidXNavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost as AndroidXNavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.get
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavEventNavigator
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
@NonRestartableComposable
public fun NavHost(
    startRoute: NavRoot,
    destinations: Set<NavDestination>,
    modifier: Modifier = Modifier,
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
    navEventNavigator: NavEventNavigator? = null,
    destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
    transitionAnimations: NavHostTransitionAnimations = NavHostDefaults.transitionAnimations(),
) {
    InternalNavHost(
        startRoute = startRoute,
        destinations = destinations,
        modifier = modifier,
        deepLinkHandlers = deepLinkHandlers,
        deepLinkPrefixes = deepLinkPrefixes,
        navEventNavigator = navEventNavigator,
        destinationChangedCallback = destinationChangedCallback,
        transitionAnimations = transitionAnimations,
    )
}

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
@NonRestartableComposable
@Deprecated("Will eventually be removed. The start destination should use a NavRoot")
public fun NavHost(
    startRoute: NavRoute,
    destinations: Set<NavDestination>,
    modifier: Modifier = Modifier,
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
    navEventNavigator: NavEventNavigator? = null,
    destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
    transitionAnimations: NavHostTransitionAnimations = NavHostDefaults.transitionAnimations(),
) {
    InternalNavHost(
        startRoute = startRoute,
        destinations = destinations,
        modifier = modifier,
        deepLinkHandlers = deepLinkHandlers,
        deepLinkPrefixes = deepLinkPrefixes,
        navEventNavigator = navEventNavigator,
        destinationChangedCallback = destinationChangedCallback,
        transitionAnimations = transitionAnimations,
    )
}

@Composable
private fun InternalNavHost(
    startRoute: BaseRoute,
    destinations: Set<NavDestination>,
    modifier: Modifier = Modifier,
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
    navEventNavigator: NavEventNavigator? = null,
    destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
    transitionAnimations: NavHostTransitionAnimations = NavHostDefaults.transitionAnimations(),
) {
    val context = LocalContext.current

    // should be called before rememberNavController to fill intent data with deeplinks
    DisposableEffect(context, deepLinkHandlers, deepLinkPrefixes) {
        context.findActivity().handleDeepLink(deepLinkHandlers, deepLinkPrefixes)
        onDispose { }
    }

    val overlayNavigator = remember { OverlayNavigator() }
    val customActivityNavigator = remember(context) { CustomActivityNavigator(context) }
    val navController: NavHostController = rememberNavController(overlayNavigator, customActivityNavigator)

    val executor = remember(navController) { AndroidXNavigationExecutor(navController) }

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
        }
    }

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

@InternalNavigationApi
public val LocalNavigationExecutor: ProvidableCompositionLocal<NavigationExecutor> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
