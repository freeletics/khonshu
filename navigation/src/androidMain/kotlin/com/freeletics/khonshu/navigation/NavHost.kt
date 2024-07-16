package com.freeletics.khonshu.navigation

import android.view.animation.PathInterpolator
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import java.io.Closeable
import java.lang.ref.WeakReference
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CancellationException

/**
 * Create a new `NavHost` containing all given [destinations]. [startRoute] will be used as the
 * start destination of the graph. Use [com.freeletics.khonshu.navigation.NavEventNavigator] and
 * [NavigationSetup] to change what is shown in [NavHost].
 *
 * To support deep links a set of [DeepLinkHandlers][DeepLinkHandler] can be passed in optionally.
 * These will be used to build the correct back stack when the current `Activity` was launched with
 * an `ACTION_VIEW` `Intent` that contains an url in it's data. [deepLinkPrefixes] can be used to
 * provide a default set of url patterns that should be matched by any [DeepLinkHandler] that
 * doesn't provide its own [DeepLinkHandler.prefixes].
 *
 * The [destinationChangedCallback] can be used to be notified when the current destination
 * changes. Note that this will not be invoked when navigating to a [ActivityRoute].
 */
@Composable
public fun NavHost(
    startRoute: NavRoot,
    destinations: ImmutableSet<NavDestination>,
    modifier: Modifier = Modifier,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler> = persistentSetOf(),
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix> = persistentSetOf(),
    destinationChangedCallback: ((NavRoot, BaseRoute) -> Unit)? = null,
) {
    val navigator = rememberHostNavigator(startRoute, destinations, deepLinkHandlers, deepLinkPrefixes)
    NavHost(navigator, modifier, destinationChangedCallback)
}

/**
 * Create a new `NavHost` with the given [HostNavigator]. The start [NavRoot], available
 * destinations and deep link handling are all dependent on the `navigator`. For more see
 * [rememberHostNavigator].
 *
 * The [destinationChangedCallback] can be used to be notified when the current destination
 * changes. Note that this will not be invoked when navigating to a [ActivityRoute].
 */
@Composable
public fun NavHost(
    navigator: HostNavigator,
    modifier: Modifier = Modifier,
    destinationChangedCallback: ((NavRoot, BaseRoute) -> Unit)? = null,
) {
    val snapshot by navigator.snapshot

    val backProgress by systemBackHandling(snapshot, navigator)
    val showPreviousEntry by remember(snapshot) {
        derivedStateOf { backProgress > 0 }
    }
    DestinationChangedCallback(snapshot, destinationChangedCallback)

    val saveableStateHolder = rememberSaveableStateHolder()
    CompositionLocalProvider(LocalHostNavigator provides navigator) {
        Box(modifier = modifier) {
            // only add previous to composition while a back gesture is ongoing
            if (showPreviousEntry) {
                Box(modifier = Modifier.inTransition { backProgress }) {
                    Show(snapshot, snapshot.previous, saveableStateHolder)
                }
            }

            snapshot.forEachVisibleDestination {
                Box(modifier = Modifier.outTransition { backProgress }) {
                    Show(snapshot, it, saveableStateHolder)
                }
            }
        }
    }
}

@Composable
private fun <T : BaseRoute> Show(
    snapshot: StackSnapshot,
    entry: StackEntry<T>,
    saveableStateHolder: SaveableStateHolder,
) {
    // From AndroidX Navigation:
    //   Stash a reference to the SaveableStateHolder in the Store so that
    //   it is available when the destination is cleared. Which, because of animations,
    //   only happens after this leaves composition. Which means we can't rely on
    //   DisposableEffect to clean up this reference (as it'll be cleaned up too early)
    val saveableCloseable = remember(entry, saveableStateHolder) {
        entry.store.getOrCreate(SaveableCloseable::class) {
            SaveableCloseable(entry.id.value)
        }
    }
    saveableCloseable.saveableStateHolderRef = WeakReference(saveableStateHolder)

    saveableStateHolder.SaveableStateProvider(entry.id.value) {
        entry.destination.content(snapshot, entry)
    }
}

internal class SaveableCloseable(
    private val id: String,
) : Closeable {
    internal lateinit var saveableStateHolderRef: WeakReference<SaveableStateHolder>

    override fun close() {
        saveableStateHolderRef.get()?.removeState(id)
        saveableStateHolderRef.clear()
    }
}

/**
 * Animation for transitioning out the screen currently on top of the back stack. The specs are
 * following the "Full screen surfaces" section of the
 * [predictive back design guide](https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back#full-screen-surfaces)
 * with 2 differences:
 *
 * - Horizontal translation was added for a better feeling.
 * - The spec uses the 35% mark as the transition point between the screens where none of them is visible, while this
 *   implementation uses 3.5% (0.035f). With the original 35% value it was easily possible to have situations where
 *   the current screen is still shown but stopping the gesture would already navigate back, which results in a very
 *   weird user experience.
 */
private fun Modifier.outTransition(progress: () -> Float): Modifier = graphicsLayer {
    val interpolatedProgress = interpolator.getInterpolation(progress())
    // the current screen is only shown until the transition point is reached
    val clippedProgress = if (interpolatedProgress <= TRANSITION_POINT) {
        interpolatedProgress / TRANSITION_POINT
    } else {
        1f
    }
    // scale from 100% to 90%
    this.scaleX = lerp(1f, 0.9f, clippedProgress)
    this.scaleY = lerp(1f, 0.9f, clippedProgress)
    // fade from 100% to 0%
    this.alpha = lerp(1f, 0f, clippedProgress)
    // offset from 0dp to 24dp
    val offset = offset.toPx()
    this.translationX = lerp(0f, offset, clippedProgress)
}

/**
 * Animation for transitioning in the screen previous back stack entry that is being navigated back to. The specs are
 * following the "Full screen surfaces" section of the
 * [predictive back design guide](https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back#full-screen-surfaces)
 * with 2 differences:
 *
 * - Horizontal translation was added for a better feeling.
 * - The spec uses the 35% mark as the transition point between the screens where none of them is visible, while this
 *   implementation uses 3.5% (0.035f). With the original 35% value it was easily possible to have situations where
 *   the current screen is still shown but stopping the gesture would already navigate back, which results in a very
 *   weird user experience.
 */
private fun Modifier.inTransition(progress: () -> Float): Modifier = graphicsLayer {
    val interpolatedProgress = interpolator.getInterpolation(progress())
    // the previous screen is shown from the transition point on
    val clippedProgress = if (interpolatedProgress >= TRANSITION_POINT) {
        (interpolatedProgress - TRANSITION_POINT) / (1f - TRANSITION_POINT)
    } else {
        0f
    }
    // scale from 110% to 100%
    this.scaleX = lerp(1.1f, 1f, clippedProgress)
    this.scaleY = lerp(1.1f, 1f, clippedProgress)
    // fade from 0 to 100%
    this.alpha = lerp(0f, 1f, clippedProgress)
    // offset from -24dp to 0dp
    val offset = (-offset).toPx()
    this.translationX = lerp(offset, 0f, clippedProgress)
}

private val offset = 24.dp
private val interpolator = PathInterpolator(0.1f, 0.1f, 0f, 1f)
private const val TRANSITION_POINT = 0.035f
private const val VISIBILITY_THRESHOLD = 0.000000001f

@Composable
private fun systemBackHandling(snapshot: StackSnapshot, navigator: HostNavigator): State<Float> {
    val backProgress = remember(snapshot) {
        Animatable(0f, visibilityThreshold = VISIBILITY_THRESHOLD)
    }
    PredictiveBackHandler(enabled = snapshot.canNavigateBack) { progressFlow ->
        var finalValue = 0f
        try {
            progressFlow.collect { backEvent ->
                backProgress.snapTo(backEvent.progress)
            }
            finalValue = 1f
            backProgress.animateTo(1f)
            navigator.navigateBack()
        } catch (e: CancellationException) {
            backProgress.animateTo(0f)
        } finally {
            // make sure that the animation is not stuck at intermediate value in case animateTo gets cancelled
            backProgress.snapTo(finalValue)
        }
    }

    // needs to be called after PredictiveBackHandler because the navigator has precedence
    val backPressedDispatcher = requireNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcher available"
    }
    DisposableEffect(backPressedDispatcher) {
        backPressedDispatcher.onBackPressedDispatcher.addCallback(navigator.onBackPressedCallback)

        onDispose {
            navigator.onBackPressedCallback.remove()
        }
    }

    return backProgress.asState()
}

@Composable
private fun DestinationChangedCallback(
    snapshot: StackSnapshot,
    destinationChangedCallback: ((NavRoot, BaseRoute) -> Unit)?,
) {
    if (destinationChangedCallback != null) {
        val root = snapshot.root
        val current = snapshot.current
        DisposableEffect(destinationChangedCallback, root, current) {
            destinationChangedCallback(root.route as NavRoot, current.route)
            onDispose {}
        }
    }
}
