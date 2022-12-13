package com.freeletics.mad.whetstone.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

@InternalWhetstoneApi
public class StoreViewModel(
    public val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val storedObjects = mutableMapOf<KClass<*>, Any>()

    public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        var storedObject = storedObjects[key] as T?
        if (storedObject == null) {
            storedObject = factory()
            storedObjects[key] = storedObject
        }
        return storedObject
    }

    override fun onCleared() {
        storedObjects.forEach { (_, storedObject) ->
            if (storedObject is CloseableComponent) {
                storedObject.closeables.forEach {
                    it.close()
                }
            }
        }

        storedObjects.clear()
    }
}
