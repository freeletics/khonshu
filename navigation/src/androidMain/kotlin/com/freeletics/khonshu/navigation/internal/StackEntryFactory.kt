package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import java.util.UUID

internal class StackEntryFactory(
    private val destinations: List<NavDestination<*>>,
    private val storeHolder: StackEntryStoreHolder,
    private val idGenerator: () -> StackEntry.Id = { StackEntry.Id(UUID.randomUUID().toString()) },
) {
    constructor(
        destinations: Set<NavDestination<*>>,
        storeHolder: StackEntryStoreHolder,
    ) : this(destinations.toList(), storeHolder)

    fun <T : BaseRoute> create(route: T): StackEntry<T> {
        return create(route, idGenerator(), StackEntryState())
    }

    fun <T : BaseRoute> create(route: T, id: StackEntry.Id, savedStateHandle: StackEntryState): StackEntry<T> {
        @Suppress("UNCHECKED_CAST")
        val destination = destinations.find { it.id == route.destinationId } as NavDestination<T>
        return StackEntry(id, route, destination, savedStateHandle, storeHolder.provideStore(id))
    }
}
