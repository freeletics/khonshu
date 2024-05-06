package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.ContentDestination
import java.util.UUID

internal class StackEntryFactory(
    private val destinations: List<ContentDestination<*>>,
    private val idGenerator: () -> StackEntry.Id = { StackEntry.Id(UUID.randomUUID().toString()) },
) {
    fun <T : BaseRoute> create(route: T): StackEntry<T> {
        return create(route, idGenerator())
    }

    fun <T : BaseRoute> create(route: T, id: StackEntry.Id): StackEntry<T> {
        @Suppress("UNCHECKED_CAST")
        val destination = destinations.find { it.id == route.destinationId } as ContentDestination<T>
        return StackEntry(id, route, destination)
    }
}
