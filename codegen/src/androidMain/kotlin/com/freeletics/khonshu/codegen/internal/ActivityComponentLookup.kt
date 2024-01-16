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
import java.io.Closeable
import kotlin.reflect.KClass

@InternalCodegenApi
public interface ActivityComponentProvider {
    public fun <T> provide(scope: KClass<*>): T
}

@InternalCodegenApi
public val LocalActivityComponentProvider: ProvidableCompositionLocal<ActivityComponentProvider> =
    staticCompositionLocalOf {
        throw IllegalStateException("ActivityComponentProvider was not provided by Activity")
    }

@InternalCodegenApi
public inline fun <C : Any, reified AC : Any, P : Any> component(
    activity: ComponentActivity,
    requestedScope: KClass<*>,
    activityScope: KClass<*>,
    activityParentScope: KClass<*>,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle) -> AC,
): C {
    if (requestedScope != activityScope) {
        return activity.findComponentByScope(requestedScope)
    }
    val store = ViewModelProvider(activity, SavedStateViewModelFactory())[ActivityComponentViewModel::class.java]
    @Suppress("UNCHECKED_CAST")
    return store.getOrCreate(AC::class) {
        val parentComponent = activity.findComponentByScope<P>(activityParentScope)
        val savedStateHandle = store.savedStateHandle
        factory(parentComponent, savedStateHandle)
    } as C
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
