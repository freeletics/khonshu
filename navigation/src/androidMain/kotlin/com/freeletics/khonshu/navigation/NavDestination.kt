package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

/**
 * A destination that can be navigated to. See `NavHost` for how to configure a `NavGraph` with it.
 */
public sealed class NavDestination<Route : BaseRoute> {
    internal abstract val id: DestinationId<Route>
    internal abstract val serializer: KSerializer<Route>
    internal abstract val parent: DestinationId<*>?
    internal abstract val extra: Any?
    internal abstract val content: @Composable (StackSnapshot, StackEntry<Route>) -> Unit
}

/**
 * Creates a new [NavDestination] that represents a full screen. The class of [Route] will be used
 * as a unique identifier. The given [content] will be shown when the screen is being
 * navigated to using an instance of [Route].
 */
@Suppress("FunctionName")
public inline fun <reified Route : BaseRoute> ScreenDestination(
    noinline content: @Composable (Route) -> Unit,
): NavDestination<Route> = ScreenDestination(DestinationId(Route::class), serializer<Route>(), null, null) { _, entry ->
    content(entry.route)
}

@InternalNavigationCodegenApi
@Suppress("FunctionName")
public inline fun <reified Route : BaseRoute> ScreenDestination(
    extra: Any,
    noinline content: @Composable (StackSnapshot, StackEntry<Route>) -> Unit,
): NavDestination<Route> = ScreenDestination(DestinationId(Route::class), serializer<Route>(), null, extra, content)

@InternalNavigationCodegenApi
@Suppress("FunctionName")
@JvmName("ScreenWithParentDestination")
public inline fun <reified Route : BaseRoute, reified ParentRoute : BaseRoute> ScreenDestination(
    extra: Any,
    noinline content: @Composable (StackSnapshot, StackEntry<Route>) -> Unit,
): NavDestination<Route> = ScreenDestination(
    id = DestinationId(Route::class),
    parent = DestinationId(ParentRoute::class),
    serializer = serializer<Route>(),
    extra = extra,
    content = content,
)

@PublishedApi
internal class ScreenDestination<Route : BaseRoute>(
    override val id: DestinationId<Route>,
    override val serializer: KSerializer<Route>,
    override val parent: DestinationId<*>?,
    override val extra: Any?,
    override val content: @Composable (StackSnapshot, StackEntry<Route>) -> Unit,
) : NavDestination<Route>()

/**
 * Creates a new [NavDestination] that is shown on top a [ScreenDestination], for example a dialog or bottom sheet. The
 * class of [Route] will be used as a unique identifier. The given [content] will be shown inside the dialog window when
 * navigating to it by using an instance of [Route].
 */
@Suppress("FunctionName")
public inline fun <reified Route : NavRoute> OverlayDestination(
    noinline content: @Composable (Route) -> Unit,
): NavDestination<Route> = OverlayDestination(DestinationId(Route::class), serializer<Route>(), null, null) {
    _,
    entry,
    ->
    content(entry.route)
}

@InternalNavigationCodegenApi
@Suppress("FunctionName")
public inline fun <reified Route : NavRoute> OverlayDestination(
    extra: Any,
    noinline content: @Composable (StackSnapshot, StackEntry<Route>) -> Unit,
): NavDestination<Route> = OverlayDestination(DestinationId(Route::class), serializer<Route>(), null, extra, content)

@InternalNavigationCodegenApi
@Suppress("FunctionName")
@JvmName("OverlayWithParentDestination")
public inline fun <reified Route : NavRoute, reified ParentRoute : BaseRoute> OverlayDestination(
    extra: Any,
    noinline content: @Composable (StackSnapshot, StackEntry<Route>) -> Unit,
): NavDestination<Route> = OverlayDestination(
    id = DestinationId(Route::class),
    parent = DestinationId(ParentRoute::class),
    serializer = serializer<Route>(),
    extra = extra,
    content = content,
)

@PublishedApi
internal class OverlayDestination<Route : NavRoute>(
    override val id: DestinationId<Route>,
    override val serializer: KSerializer<Route>,
    override val parent: DestinationId<*>?,
    override val extra: Any?,
    override val content: @Composable (StackSnapshot, StackEntry<Route>) -> Unit,
) : NavDestination<Route>()
