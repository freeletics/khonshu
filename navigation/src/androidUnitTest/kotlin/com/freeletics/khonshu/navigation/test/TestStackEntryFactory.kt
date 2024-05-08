package com.freeletics.khonshu.navigation.test

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.ContentDestination
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackEntryStore
import com.freeletics.khonshu.navigation.internal.destinationId

internal class TestStackEntryFactory {
    private var nextId = 100

    private val handles = mutableMapOf<StackEntry.Id, SavedStateHandle>()
    private val stores = mutableMapOf<StackEntry.Id, StackEntryStore>()

    val closedEntries = mutableListOf<StackEntry.Id>()

    fun <T : BaseRoute> create(route: T): StackEntry<T> {
        return create(StackEntry.Id((nextId++).toString()), route)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRoute> create(id: StackEntry.Id, route: T): StackEntry<T> {
        val destination = destinations.find { it.id == route.destinationId } as ContentDestination<T>
        val handle = handles.getOrPut(id) { SavedStateHandle() }
        val store = stores.getOrPut(id) { StackEntryStore { closedEntries.add(id) } }
        return StackEntry(id, route, destination, handle, store)
    }
}
