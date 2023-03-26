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
import com.freeletics.mad.navigator.compose.internal.rememberNavigationExecutor
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.NavigationExecutor
import kotlinx.coroutines.flow.distinctUntilChanged

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

    if (destinationChangedCallback != null) {
        DisposableEffect(key1 = destinationChangedCallback) {
            // TODO start listening to backstack changes and send them to destinationChangedCallback

            onDispose {
                // TODO stop listening
            }
        }
    }

    CompositionLocalProvider(LocalNavigationExecutor provides executor) {
        // TODO show currently visible screen
        // TODO show dialog if needed
        // TODO show bottom sheet if needed
    }
}

@Composable
private fun SystemBackHandling(executor: MultiStackNavigationExecutor) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
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
        backPressedDispatcher!!.onBackPressedDispatcher.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }
}

@InternalNavigatorApi
public val LocalNavigationExecutor: ProvidableCompositionLocal<NavigationExecutor> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
