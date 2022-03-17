package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import kotlin.reflect.KClass

@InternalWhetstoneApi
public class WhetstoneViewModelFactory<D>(
    savedStateRegistryOwner: SavedStateRegistryOwner,
    private val context: Context,
    private val scope: KClass<*>,
    private val factory: (D, SavedStateHandle) -> ViewModel
) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {

    @Suppress("UNCHECKED_CAST")
    public override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val dependencies: D = findDependencies()
        return factory(dependencies, handle) as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> findDependencies(): T {
        val serviceName = scope.qualifiedName!!
        val dependencies: T? = context.getSystemService(serviceName) as T?
        if (dependencies != null) {
            return dependencies
        }
        return context.applicationContext.getSystemService(serviceName) as T
    }
}
