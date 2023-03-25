package com.freeletics.mad.navigator.compose

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberModalBottomSheetState
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
import androidx.compose.ui.window.Dialog
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.DeepLinkHandler
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.compose.internal.MultiStackNavigationExecutor
import com.freeletics.mad.navigator.compose.internal.StackEntry
import com.freeletics.mad.navigator.compose.internal.rememberNavigationExecutor
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.NavigationExecutor
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Create a new `NavHost` containing all given [destinations]. [startRoute] will be used as the
 * start destination of the graph. Use [com.freeletics.mad.navigator.NavEventNavigator] and
 * [NavigationSetup] to change what is shown in [NavHost].
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
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
        val entries = executor.visibleEntries.value

        val bottomSheetEntry = entries.lastOrNull { it.destination is BottomSheetDestination<*> }
        val modalBottomSheetState = rememberBottomSheetState(bottomSheetEntry, executor)

        ModalBottomSheetLayout(
            sheetContent = {
                if (bottomSheetEntry != null) {
                    Show(bottomSheetEntry)
                }
            },
            sheetState = modalBottomSheetState,
            sheetShape = bottomSheetShape,
            sheetElevation = bottomSheetElevation,
            sheetBackgroundColor = bottomSheetBackgroundColor,
            sheetContentColor = bottomSheetContentColor,
            scrimColor = bottomSheetScrimColor,
        ) {
            Show(entries, executor)
        }
    }
}

@Composable
@ExperimentalMaterialApi
private fun rememberBottomSheetState(
    bottomSheetEntry: StackEntry<*>?,
    executor: MultiStackNavigationExecutor
): ModalBottomSheetState {
    val modalBottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true,
    )

    LaunchedEffect(modalBottomSheetState, bottomSheetEntry) {
        if (bottomSheetEntry != null) {
            modalBottomSheetState.show()

            snapshotFlow { modalBottomSheetState.isVisible }
                .distinctUntilChanged()
                .collect {
                    if (!it) {
                        executor.navigateBack()
                    }
                }
        } else {
            modalBottomSheetState.hide()
        }
    }

    return modalBottomSheetState
}

@Composable
private fun Show(
    entries: List<StackEntry<*>>,
    executor: MultiStackNavigationExecutor,
) {
    entries.forEach { entry ->
        when(entry.destination) {
            is ScreenDestination -> {
                Show(entry)
            }
            is DialogDestination<*> -> {
                Dialog(onDismissRequest = { executor.navigateBack() }) {
                    Show(entry)
                }
            }
            // handled already in the layout
            is BottomSheetDestination<*> -> {}
        }
    }
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

@InternalNavigatorApi
public val LocalNavigationExecutor: ProvidableCompositionLocal<NavigationExecutor> = staticCompositionLocalOf {
    throw IllegalStateException("Can't use NavEventNavigationHandler outside of a navigator NavHost")
}
