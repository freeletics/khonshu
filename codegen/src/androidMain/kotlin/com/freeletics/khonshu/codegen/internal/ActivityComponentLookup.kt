package com.freeletics.khonshu.codegen.internal

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import java.io.Closeable
import kotlin.reflect.KClass

@InternalCodegenApi
public inline fun <reified C : Any, P : Any> component(
    viewModelStoreOwner: ViewModelStoreOwner,
    context: Context,
    parentScope: KClass<*>,
    arguments: Bundle?,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle, Bundle) -> C,
): C {
    val store = ViewModelProvider(
        viewModelStoreOwner,
        SavedStateViewModelFactory(),
    )[ActivityComponentViewModel::class.java]
    return store.getOrCreate(C::class) {
        val parentComponent = context.findComponentByScope<P>(parentScope)
        val savedStateHandle = store.savedStateHandle
        factory(parentComponent, savedStateHandle, arguments ?: Bundle.EMPTY)
    }
}

@PublishedApi
@InternalCodegenApi
internal class ActivityComponentViewModel(
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val storedObjects = mutableMapOf<KClass<*>, Any>()

    fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        var storedObject = storedObjects[key] as T?
        if (storedObject == null) {
            storedObject = factory()
            storedObjects[key] = storedObject
            if (storedObject is Closeable) {
                addCloseable(storedObject)
            }
        }
        return storedObject
    }

    override fun onCleared() {
        storedObjects.clear()
    }
}

@PublishedApi
internal fun <T> Context.findComponentByScope(scope: KClass<*>): T {
    val serviceName = scope.qualifiedName!!
    val component = getSystemService(serviceName) ?: applicationContext.getSystemService(serviceName)
    checkNotNull(component) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return component as T
}
