package com.freeletics.mad.navigator.internal

import androidx.lifecycle.ViewModel
import kotlin.reflect.KClass

internal class StoreViewModel : ViewModel(), NavigationExecutor.Store {
    private val storedObjects = mutableMapOf<KClass<*>, Any>()

    override fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T {
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
