package com.freeletics.mad.whetstone.internal

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

@InternalWhetstoneApi
public class WhetstoneViewModelFactory(
    savedStateRegistryOwner: SavedStateRegistryOwner,
    private val factory: (SavedStateHandle) -> ViewModel
) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {

    @Suppress("UNCHECKED_CAST")
    public override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return factory(handle) as T
    }
}
