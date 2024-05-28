package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

internal class StackEntryStoreViewModel(
    internal val globalSavedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val stores = mutableMapOf<StackEntry.Id, StackEntryStore>()

    fun provideStore(id: StackEntry.Id): StackEntryStore {
        return stores.getOrPut(id) {
            StackEntryStore { stores.remove(id) }
        }
    }

    private fun removeStore(id: StackEntry.Id) {
        stores.remove(id)
    }

    public override fun onCleared() {
        while (stores.isNotEmpty()) {
            val key = stores.firstNotNullOf { it.key }
            stores.remove(key)?.close()
        }
    }
}
