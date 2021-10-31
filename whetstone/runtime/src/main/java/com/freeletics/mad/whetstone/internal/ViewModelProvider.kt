package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import kotlin.reflect.KClass

/**
 * Creates a [ViewModelProvider] for the given [fragment] that uses a custom
 * [AbstractSavedStateViewModelFactory]. That factory will use [scope] to lookup the required
 * dependencies interface [D] implementation from either the `Activity` or the `Application`
 * `Context`. The [D] instance will then, together with the [SavedStateHandle] be passed to
 * [factory] to instantiate the [ViewModel].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
@Composable
public fun <D> rememberViewModelProvider(
    scope: KClass<*>,
    factory: (D, SavedStateHandle) -> ViewModel
): ViewModelProvider {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val context = LocalContext.current
    return remember(scope, viewModelStoreOwner, savedStateRegistryOwner) {
        val viewModelFactory = SimpleSavedStateViewModelFactory(savedStateRegistryOwner, context, scope, factory)
        ViewModelProvider(viewModelStoreOwner, viewModelFactory)
    }
}

/**
 * Creates a [ViewModelProvider] for the given [entry] that uses a custom
 * [AbstractSavedStateViewModelFactory]. That factory will use [scope] to lookup a parent
 * component [C] instance from either the `Activity` or the `Application`
 * `Context`. The [C] instance will then, together with the [SavedStateHandle] be passed to
 * [factory] to instantiate the [ViewModel].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
public fun <C> viewModelProvider(
    entry: NavBackStackEntry,
    context: Context,
    scope: KClass<*>,
    factory: (C, SavedStateHandle) -> ViewModel
): ViewModelProvider {
    val viewModelFactory = SimpleSavedStateViewModelFactory(entry, context, scope, factory)
    return ViewModelProvider(entry, viewModelFactory)
}

@InternalWhetstoneApi
public class SimpleSavedStateViewModelFactory<D>(
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
