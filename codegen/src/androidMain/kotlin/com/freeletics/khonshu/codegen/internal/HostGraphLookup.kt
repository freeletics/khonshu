package com.freeletics.khonshu.codegen.internal

import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.freeletics.khonshu.codegen.GlobalGraphProvider
import kotlin.reflect.KClass

@InternalCodegenApi
public interface HostGraphProvider {
    public fun <T> provide(scope: KClass<*>): T
}

@InternalCodegenApi
public val LocalHostGraphProvider: ProvidableCompositionLocal<HostGraphProvider> =
    staticCompositionLocalOf {
        throw IllegalStateException("HostGraphProvider was not provided")
    }

@InternalCodegenApi
public inline fun <C : Any, reified AC : Any, P : Any> getGraph(
    viewModelStoreOwner: ViewModelStoreOwner,
    globalGraphProvider: GlobalGraphProvider,
    requestedScope: KClass<*>,
    activityScope: KClass<*>,
    activityParentScope: KClass<*>,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle) -> AC,
): C {
    if (requestedScope != activityScope) {
        return globalGraphProvider.getGraph(requestedScope)
    }
    val store = ViewModelProvider(viewModelStoreOwner, SavedStateViewModelFactory())[ActivityGraphViewModel::class.java]
    @Suppress("UNCHECKED_CAST")
    return store.getOrCreate(AC::class) {
        val parentGraph = globalGraphProvider.getGraph<P>(activityParentScope)
        val savedStateHandle = store.savedStateHandle
        factory(parentGraph, savedStateHandle)
    } as C
}

@PublishedApi
@InternalCodegenApi
internal class ActivityGraphViewModel(
    val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val storedObjects = mutableMapOf<KClass<*>, Any>()

    fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T {
        @Suppress("UNCHECKED_CAST")
        var storedObject = storedObjects[key] as T?
        if (storedObject == null) {
            storedObject = factory()
            storedObjects[key] = storedObject
            if (storedObject is AutoCloseable) {
                addCloseable(storedObject)
            }
        }
        return storedObject
    }

    override fun onCleared() {
        storedObjects.clear()
    }
}
