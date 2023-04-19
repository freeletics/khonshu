package com.freeletics.mad.navigator.compose

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.compose.internal.MultiStackNavigationExecutor
import com.freeletics.mad.navigator.compose.internal.StackEntry
import com.freeletics.mad.navigator.compose.internal.rememberNavigationExecutor
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.NavigationExecutor
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Create a new `NavHost containing all given [destinations]. [startRoute] will be used as the
 * start destination of the graph. Use [com.freeletics.mad.navigator.NavEventNavigator] and
 * [NavigationSetup] to chage what is shown in [NavHost]
 *
 * The [destinationChangedCallback] can be used to be notified when the current destination
 * changes. Note that this will not be invoked when navigating to a [ActivityDestination].
 */
@Composable
@Suppress("unused_parameter") //TODO
public fun NavHost(
    startRoute: NavRoot,
    destinations: Set<NavDestination>,
    deepLinkHandlers: Set<DeepLinkHandler> = emptySet(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = emptySet(),
    destinationChangedCallback: ((BaseRoute) -> Unit)? = null,
    bottomSheetShape: Shape = MaterialTheme.shapes.large,
    bottomSheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    bottomSheetBackgroundColor: Color = MaterialTheme.colors.surface,
    bottomSheetContentColor: Color = contentColorFor(bottomSheetBackgroundColor),
    bottomSheetScrimColor: Color = ModalBottomSheetDefaults.scrimColor,
) {
    val executor = rememberNavigationExecutor(startRoute, destinations, deepLinkHandlers, deepLinkPrefixes)

    SystemBackHandling(executor)
    DestinationChangedCallback(executor, destinationChangedCallback)

    CompositionLocalProvider(LocalNavigationExecutor provides executor) {
        val entries = executor.visibleEntries.value

        Show(entries)
    }
}

@Composable
private fun Show(
    entries: List<StackEntry<*>>,
) {
    // TODO show all entries and differentiate between destination types
    val entry = entries.last()
    Show(entry)
}

@Composable
private fun <T : BaseRoute> Show(
    entry: StackEntry<T>,
) {
    entry.destination.content(entry.route)
}

@Composable
private fun SystemBackHandling(executor: MultiStackNavigationExecutor) {
    val backPressedDispatcher = requireNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcher available"
    }

    val callback = remember(executor) {
        object : OnBackPressedCallback(executor.canNavigateBack.value) {
            override fun handleOnBackPressed() {
                executor.navigateBack()
            }

        }
    }

    LaunchedEffect(executor, callback) {
        snapshotFlow { executor.canNavigateBack.value }
            .distinctUntilChanged()
            .collect { callback.isEnabled = it }
    }

    DisposableEffect(backPressedDispatcher, callback) {
        backPressedDispatcher.onBackPressedDispatcher.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }
}

@Composable
private fun DestinationChangedCallback(
    executor: MultiStackNavigationExecutor,
    destinationChangedCallback: ((BaseRoute) -> Unit)?
) {
    if (destinationChangedCallback != null) {
        LaunchedEffect(executor, destinationChangedCallback) {
            snapshotFlow { executor.visibleEntries.value }
                .map { it.last().route }
                .distinctUntilChanged()
                .collect { destinationChangedCallback(it) }
        }
    }
}

@InternalNavigatorApi
public val LocalNavigationExecutor: ProvidableCompositionLocal<NavigationExecutor> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
