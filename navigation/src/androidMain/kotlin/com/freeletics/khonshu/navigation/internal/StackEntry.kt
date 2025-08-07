package com.freeletics.khonshu.navigation.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.movableContentOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.ContentDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.OverlayDestination
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
@InternalNavigationCodegenApi
public class StackEntry<T : BaseRoute> internal constructor(
    internal val id: Id,
    public val route: T,
    private val destination: ContentDestination<T>,
    public val savedStateHandle: SavedStateHandle,
    public val store: StackEntryStore,
) {
    internal val destinationId
        get() = route.destinationId

    internal val isOverlay
        get() = destination is OverlayDestination<*>

    internal val parentDesintationId: DestinationId<*>?
        get() = destination.parent

    @InternalNavigationCodegenApi
    public val extra: Any?
        get() = destination.extra

    internal val removable
        // cast is needed for the compiler to recognize that the when is exhaustive
        @Suppress("USELESS_CAST")
        get() = when (route as BaseRoute) {
            is NavRoute -> true
            is NavRoot -> false
        }

    internal fun content(snapshot: StackSnapshot): @Composable (Modifier) -> Unit = movableContentOf<Modifier> {
        Box(modifier = it) {
            destination.content(snapshot, this@StackEntry)
        }
    }

    internal fun close() {
        store.close()
    }

    @JvmInline
    internal value class Id(internal val value: String)
}
