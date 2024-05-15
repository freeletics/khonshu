package com.freeletics.khonshu.navigation.internal

import java.io.Closeable
import kotlin.reflect.KClass

@InternalNavigationCodegenApi
public class StackEntryStore(
    private val onClose: () -> Unit,
) : Closeable {
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

    override fun close() {
        onClose()
        storedObjects.forEach { (_, storedObject) ->
            if (storedObject is Closeable) {
                storedObject.close()
            }
        }
        storedObjects.clear()
    }
}
