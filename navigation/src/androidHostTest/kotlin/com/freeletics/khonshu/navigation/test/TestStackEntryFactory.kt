package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavDestination
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackEntryState
import com.freeletics.khonshu.navigation.internal.StackEntryStore
import com.freeletics.khonshu.navigation.internal.destinationId

internal class TestStackEntryFactory {
    private var nextId = 100

    private val handles = mutableMapOf<StackEntry.Id, StackEntryState>()
    private val stores = mutableMapOf<StackEntry.Id, StackEntryStore>()

    val closedEntries = mutableListOf<StackEntry.Id>()

    fun <T : BaseRoute> create(route: T): StackEntry<T> {
        return create(StackEntry.Id((nextId++).toString()), route)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRoute> create(id: StackEntry.Id, route: T): StackEntry<T> {
        val destination = destinations.find { it.id == route.destinationId } as NavDestination<T>
        val handle = handles.getOrPut(id) { StackEntryState() }
        val store = stores.getOrPut(id) { StackEntryStore { closedEntries.add(id) } }
        return StackEntry(id, route, destination, handle, store)
    }
}
