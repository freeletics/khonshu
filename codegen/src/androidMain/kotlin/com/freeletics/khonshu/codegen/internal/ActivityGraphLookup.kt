package com.freeletics.khonshu.codegen.internal

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

@InternalCodegenApi
public interface ActivityGraphProvider {
    public fun <T> provide(scope: KClass<*>): T
}

@InternalCodegenApi
public val LocalActivityGraphProvider: ProvidableCompositionLocal<ActivityGraphProvider> =
    staticCompositionLocalOf {
        throw IllegalStateException("ActivityGraphProvider was not provided by Activity")
    }

@InternalCodegenApi
public inline fun <C : Any, reified AC : Any, P : Any> getGraph(
    activity: ComponentActivity,
    requestedScope: KClass<*>,
    activityScope: KClass<*>,
    activityParentScope: KClass<*>,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle) -> AC,
): C {
    if (requestedScope != activityScope) {
        return activity.findGraphByScope(requestedScope)
    }
    val store = ViewModelProvider(activity, SavedStateViewModelFactory())[ActivityGraphViewModel::class.java]
    @Suppress("UNCHECKED_CAST")
    return store.getOrCreate(AC::class) {
        val parentGraph = activity.findGraphByScope<P>(activityParentScope)
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

@PublishedApi
internal fun <T> Context.findGraphByScope(scope: KClass<*>): T {
    val serviceName = scope.qualifiedName!!
    val graph = getSystemService(serviceName) ?: applicationContext.getSystemService(serviceName)
    checkNotNull(graph) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return graph as T
}
