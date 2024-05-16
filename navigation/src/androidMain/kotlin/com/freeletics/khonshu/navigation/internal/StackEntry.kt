package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.ContentDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import dev.drewhamilton.poko.Poko

@Poko
@Immutable
@InternalNavigationCodegenApi
public class StackEntry<T : BaseRoute> internal constructor(
    internal val id: Id,
    public val route: T,
    internal val destination: ContentDestination<T>,
    public val savedStateHandle: SavedStateHandle,
    public val store: StackEntryStore,
) {
    internal val destinationId
        get() = route.destinationId

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

    fun close() {
        store.close()
    }

    @JvmInline
    internal value class Id(internal val value: String)
}
