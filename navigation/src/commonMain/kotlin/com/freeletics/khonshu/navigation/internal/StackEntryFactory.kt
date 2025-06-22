package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import java.util.UUID
import kotlinx.collections.immutable.ImmutableSet

internal class StackEntryFactory(
    private val destinations: List<NavDestination<*>>,
    private val storeHolder: StackEntryStoreHolder,
    private val idGenerator: () -> StackEntry.Id = { StackEntry.Id(UUID.randomUUID().toString()) },
) {
    constructor(
        destinations: ImmutableSet<NavDestination<*>>,
        storeHolder: StackEntryStoreHolder,
    ) : this(destinations.toList(), storeHolder)

    fun <T : BaseRoute> create(route: T): StackEntry<T> {
        return create(route, idGenerator(), SavedStateHandle())
    }

    fun <T : BaseRoute> create(route: T, id: StackEntry.Id, savedStateHandle: SavedStateHandle): StackEntry<T> {
        @Suppress("UNCHECKED_CAST")
        val destination = destinations.find { it.id == route.destinationId } as NavDestination<T>
        return StackEntry(id, route, destination, savedStateHandle, storeHolder.provideStore(id))
    }
}
