package com.freeletics.khonshu.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
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
import com.freeletics.khonshu.navigation.internal.PredictiveBackHandler
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import java.io.Closeable
import java.lang.ref.WeakReference
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException

/**
 * Create a new `NavHost` containing all given [destinations]. [startRoute] will be used as the
 * start destination of the graph. Use [HostNavigator] and [DestinationNavigator] to change what is shown
 * in [NavHost].
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
    val currentSnapshot by navigator.snapshot
    val snapshot: StackSnapshot

    val gestureBackProgress = systemBackHandling(currentSnapshot, navigator)
    val backProgress: State<Float>

    // use a transition to complete the back animation after back was committed and the state updated
    val transition = updateTransition(TransitionState(currentSnapshot, gestureBackProgress), "NavHost")
    // when going back (stack is smaller) and the old state has an in progress back animation
    if (transition.targetState.snapshot.size < transition.currentState.snapshot.size &&
        transition.currentState.backProgress.value != 0f
    ) {
        // animate from current value to complete the back animation
        backProgress = transition.animateFloat(label = "back-progress") { it.backProgressTargetValue }
        // for now still use the old snapshot
        snapshot = transition.currentState.snapshot
    } else {
        backProgress = transition.targetState.backProgress
        snapshot = transition.targetState.snapshot
    }

    // `backProgress` should not directly be accessed in the composition to avoid a re-composition on every small
    // progress update during the gesture. To achieve that `showPreviousEntry` uses `derivedStateOf` which will
    // limit re-compositions to when the boolean changes. For the animation the value is only accessed from within
    // the `graphicsLayer {}` block.
    val showPreviousEntry by remember(snapshot) {
        derivedStateOf { backProgress.value > 0 }
    }

    DestinationChangedCallback(currentSnapshot, destinationChangedCallback)

    // Remember the movableContent functions from the individual entries so that we avoid blinking at the end of
    // the predictive back animations.
    val entryComposables = remember { mutableMapOf<StackEntry.Id, @Composable () -> Unit>() }
    // build list of show-able entries that are visible in this composition
    val entries = snapshot.getShowableEntries(
        entryComposables,
        showPreviousEntry,
        Modifier.inTransition { backProgress.value },
        Modifier.outTransition { backProgress.value },
    )

    val saveableStateHolder = rememberSaveableStateHolder()
    CompositionLocalProvider(LocalHostNavigator provides navigator) {
        Box(modifier = modifier) {
            entries.forEach {
                Show(it, saveableStateHolder)
            }
        }
    }
}

@Stable
private data class TransitionState(
    val snapshot: StackSnapshot,
    val backProgress: State<Float>,
) {
    /**
     * Calculates the target values for [Transition.animateFloat]. If the current back progress value is
     * not 0 it is returned which will be the start value of the transition. The value for the target
     * state needs to be 1 so that the progress animation is completed.
     *
     * This makes the assumption that it's never called for aborting the animation where 0 would need
     * to be returned.
     */
    val backProgressTargetValue: Float
        get() {
            val value = backProgress.value
            return if (value != 0f) {
                value
            } else {
                1f
            }
        }
}

private fun StackSnapshot.getShowableEntries(
    entryComposables: MutableMap<StackEntry.Id, @Composable () -> Unit>,
    showPreviousEntry: Boolean,
    inTransition: Modifier,
    outTransition: Modifier,
): ImmutableList<ShowableStackEntry<*>> {
    val entries = mutableListOf<ShowableStackEntry<*>>()
    val previous = previous
    // only add previous to composition while a back gesture is ongoing
    if (showPreviousEntry && previous != null) {
        entries += ShowableStackEntry(
            entry = previous,
            modifier = inTransition,
            content = entryComposables.getOrPut(previous.id) { previous.content(this) },
        )
    }
    forEachVisibleDestination {
        entries += ShowableStackEntry(
            entry = it,
            modifier = outTransition,
            content = entryComposables.getOrPut(it.id) { it.content(this) },
        )
    }

    // update entryComposables to remove any entry that left the composition
    entryComposables.clear()
    entries.forEach {
        entryComposables.put(it.entry.id, it.content)
    }

    return entries.toImmutableList()
}

@Immutable
private data class ShowableStackEntry<T : BaseRoute>(
    val entry: StackEntry<T>,
    val modifier: Modifier,
    val content: @Composable () -> Unit,
)

@Composable
private fun <T : BaseRoute> Show(
    entry: ShowableStackEntry<T>,
    saveableStateHolder: SaveableStateHolder,
) {
    // From AndroidX Navigation:
    //   Stash a reference to the SaveableStateHolder in the Store so that
    //   it is available when the destination is cleared. Which, because of animations,
    //   only happens after this leaves composition. Which means we can't rely on
    //   DisposableEffect to clean up this reference (as it'll be cleaned up too early)
    val saveableCloseable = remember(entry, saveableStateHolder) {
        entry.entry.store.getOrCreate(SaveableCloseable::class) {
            SaveableCloseable(entry.entry.id.value)
        }
    }
    saveableCloseable.saveableStateHolderRef = WeakReference(saveableStateHolder)

    saveableStateHolder.SaveableStateProvider(entry.entry.id.value) {
        Box(modifier = entry.modifier) {
            entry.content()
        }
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
    val interpolatedProgress = interpolator.transform(progress())
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
    val interpolatedProgress = interpolator.transform(progress())
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
private val interpolator = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
private const val TRANSITION_POINT = 0.035f
private const val VISIBILITY_THRESHOLD = 0.000000001f

@Composable
private fun systemBackHandling(snapshot: StackSnapshot, navigator: HostNavigator): State<Float> {
    val backProgress = remember(snapshot) {
        Animatable(0f, visibilityThreshold = VISIBILITY_THRESHOLD)
    }
    PredictiveBackHandler(
        enabled = snapshot.canNavigateBack,
        extraCallback = navigator.onBackPressedCallback,
    ) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                backProgress.snapTo(backEvent.progress)
            }
            navigator.tryNavigateBack()
        } catch (e: CancellationException) {
            backProgress.tryAnimateTo(0f)
        }
    }

    return backProgress.asState()
}

private fun HostNavigator.tryNavigateBack() {
    try {
        navigateBack()
    } catch (e: IllegalStateException) {
        // The exception is thrown when navigateBack is called while the backstack is at the root. If the
        // system back is triggered twice very quickly after each other there is a short time window
        // after the first where the OnBackPressedCallback is not yet updated and would then also handle the
        // second. This suppresses the crash in that case.
        if (snapshot.value.canNavigateBack) {
            throw e
        }
    }
}

private suspend fun Animatable<Float, *>.tryAnimateTo(value: Float) {
    try {
        animateTo(value)
    } catch (_: CancellationException) {
        // make sure that the animation is not stuck at intermediate value in case animateTo gets cancelled
        snapTo(value)
    }
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
