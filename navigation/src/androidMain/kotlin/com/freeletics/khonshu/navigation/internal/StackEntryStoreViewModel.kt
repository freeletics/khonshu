package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

internal class StackEntryStoreViewModel(
    internal val globalSavedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val stores = mutableMapOf<StackEntry.Id, StackEntryStore>()

    fun provideStore(id: StackEntry.Id): StackEntryStore {
        return stores.getOrPut(id) { StackEntryStore() }
    }

    fun removeEntry(id: StackEntry.Id) {
        val store = stores.remove(id)
        store?.close()
    }

    public override fun onCleared() {
        for (store in stores.values) {
            store.close()
        }
        stores.clear()
    }
}
