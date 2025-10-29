package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import java.util.UUID

internal class StackEntryFactory(
    private val destinations: List<NavDestination<*>>,
    private val viewModel: StackEntryStoreViewModel,
    private val idGenerator: () -> StackEntry.Id = { StackEntry.Id(UUID.randomUUID().toString()) },
) {
    fun <T : BaseRoute> create(route: T): StackEntry<T> {
        return create(route, idGenerator(), SavedStateHandle())
    }

    fun <T : BaseRoute> create(route: T, id: StackEntry.Id, savedStateHandle: SavedStateHandle): StackEntry<T> {
        @Suppress("UNCHECKED_CAST")
        val destination = destinations.find { it.id == route.destinationId } as NavDestination<T>
        return StackEntry(id, route, destination, savedStateHandle, viewModel.provideStore(id))
    }
}
