package com.freeletics.mad.navigator.compose

import android.content.Intent
import androidx.compose.runtime.Composable
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.internal.ActivityDestinationId
import com.freeletics.mad.navigator.internal.DestinationId

/**
 * A destination that can be navigated to. See [NavHost] for how to configure a `NavGraph` with it.
 */
public sealed interface NavDestination

internal sealed interface ContentDestination<T : BaseRoute> : NavDestination {
    val id: DestinationId<T>
    val content: @Composable (T) -> Unit
}

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [T] will be used
 * as a unique identifier. The given [content] will be shown when the screen is being
 * navigated to using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : BaseRoute> ScreenDestination(
    noinline content: @Composable (T) -> Unit,
): NavDestination = ScreenDestination(DestinationId(T::class), content)

@PublishedApi
internal class ScreenDestination<T : BaseRoute>(
    override val id: DestinationId<T>,
    override val content: @Composable (T) -> Unit,
) : ContentDestination<T>

/**
 * Creates a new [NavDestination] that represents a dialog. The class of [T] will be used
 * as a unique identifier. The given [content] will be shown inside the dialog window when
 * navigating to it by using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> DialogDestination(
    noinline content: @Composable (T) -> Unit,
): NavDestination = DialogDestination(DestinationId(T::class), content)

@PublishedApi
internal class DialogDestination<T : NavRoute>(
    override val id: DestinationId<T>,
    override val content: @Composable (T) -> Unit,
) : ContentDestination<T>

/**
 * Creates a new [NavDestination] that represents a bottom sheet. The class of [T] will be used
 * as a unique identifier. The given [content] will be shown inside the bottom sheet
 * when navigating to it by using an instance of [T].
 */
@Suppress("FunctionName")
public inline fun <reified T : NavRoute> BottomSheetDestination(
    noinline content: @Composable (T) -> Unit,
): NavDestination = BottomSheetDestination(DestinationId(T::class), content)

@PublishedApi
internal class BottomSheetDestination<T : NavRoute>(
    override val id: DestinationId<T>,
    override val content: @Composable (T) -> Unit,
) : ContentDestination<T>

/**
 * Creates a new [NavDestination] that represents an `Activity`. The class of [T] will be used
 * as a unique identifier. The given [intent] will be used to launch the `Activity` when using an
 * instance of [T] for navigation.
 */
@Suppress("FunctionName")
public inline fun <reified T : ActivityRoute> ActivityDestination(
    intent: Intent,
): NavDestination = ActivityDestination(ActivityDestinationId(T::class), intent)

@PublishedApi
internal class ActivityDestination(
    internal val id: ActivityDestinationId<out ActivityRoute>,
    internal val intent: Intent,
) : NavDestination
