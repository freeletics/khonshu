package com.freeletics.mad.navigator.compose.internal

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.freeletics.mad.navigator.internal.NavigationExecutor
import com.freeletics.mad.navigator.internal.NavigationExecutorStore

internal class StoreViewModel(
    internal val globalSavedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stores = mutableMapOf<StackEntry.Id, NavigationExecutorStore>()
    private val savedStateHandles = mutableMapOf<StackEntry.Id, SavedStateHandle>()

    fun provideStore(id: StackEntry.Id): NavigationExecutor.Store {
        var store = stores[id]
        if (store == null) {
            store = NavigationExecutorStore()
            stores[id] = store
        }
        return store
    }

    @SuppressLint("RestrictedApi")
    fun provideSavedStateHandle(id: StackEntry.Id): SavedStateHandle {
        var savedStateHandle = savedStateHandles[id]
        if (savedStateHandle == null) {
            val restoredBundle = globalSavedStateHandle.get<Bundle>(id.value)
            savedStateHandle = SavedStateHandle.createHandle(restoredBundle, null)
            globalSavedStateHandle.setSavedStateProvider(id.value, savedStateHandle.savedStateProvider())
            savedStateHandles[id] = savedStateHandle
        }
        return savedStateHandle
    }

    fun removeEntry(id: StackEntry.Id) {
        val store = stores.remove(id)
        store?.close()

        savedStateHandles.remove(id)
        globalSavedStateHandle.clearSavedStateProvider(id.value)
        globalSavedStateHandle.remove<Any>(id.value)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onCleared() {
        for (store in stores.values) {
            store.close()
        }
        stores.clear()

        for (key in savedStateHandles.keys) {
            globalSavedStateHandle.clearSavedStateProvider(key.value)
            globalSavedStateHandle.remove<Any>(key.value)
        }
        savedStateHandles.clear()
    }
}
