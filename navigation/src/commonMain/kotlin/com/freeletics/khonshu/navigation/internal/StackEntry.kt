package com.freeletics.khonshu.navigation.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.saveable.Saver as ComposeSaver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.OverlayDestination
import com.freeletics.khonshu.navigation.ScreenDestination
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable

@Poko
@Immutable
@InternalNavigationCodegenApi
public class StackEntry<T : BaseRoute> internal constructor(
    internal val id: Id,
    public val route: T,
    private val destination: NavDestination<T>,
    public val state: StackEntryState,
    public val store: StackEntryStore,
) {
    internal val destinationId
        get() = route.destinationId

    internal val isOverlay
        get() = destination is OverlayDestination<*>

    internal val parentDesintationId: DestinationId<*>?
        get() = destination.parent

    // for now to be compatible with codegen and not expose StackEntryState publicly yet
    @InternalNavigationCodegenApi
    public val savedStateHandle: SavedStateHandle
        get() = state.savedStateHandle()

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
    @Serializable
    @InternalNavigationTestingApi
    public value class Id(internal val value: String)

    @InternalNavigationTestingApi
    public companion object {
        @InternalNavigationTestingApi
        public fun create(id: Id, route: BaseRoute): StackEntry<BaseRoute> {
            return StackEntry(
                id = id,
                route = route,
                destination = ScreenDestination<BaseRoute> {},
                state = StackEntryState(),
                store = StackEntryStore {},
            )
        }

        private const val KEY_ID = "id"
        private const val KEY_ROUTE = "route"
        private const val KEY_STATE = "state"
    }

    internal class Saver(
        private val createRestoredEntry: (BaseRoute, Id, StackEntryState) -> StackEntry<*>,
        private val savedStateConfiguration: SavedStateConfiguration,
    ) : ComposeSaver<StackEntry<*>, SavedState> {
        override fun restore(value: SavedState): StackEntry<*> {
            return value.read {
                createRestoredEntry(
                    decodeFromSavedState(getSavedState(KEY_ROUTE), savedStateConfiguration),
                    Id(getString(KEY_ID)),
                    StackEntryState(getSavedState(KEY_STATE)),
                )
            }
        }

        override fun SaverScope.save(value: StackEntry<*>): SavedState = savedState {
            putString(KEY_ID, value.id.value)
            putSavedState(KEY_ROUTE, encodeToSavedState(value.route, savedStateConfiguration))
            putSavedState(KEY_STATE, value.state.saveState())
        }
    }
}
