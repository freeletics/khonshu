package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import kotlin.reflect.KClass

/**
 * Creates a [ViewModelProvider] for the gived [fragment] that uses a custom
 * [AbstractSavedStateViewModelFactory]. That factory will use [scope] to lookup the required
 * dependencies interface [D] implementation from either the `Activity` or the `Application`
 * `Context`. The [D] instance will then, together with the [SavedStateHandle] be passed to
 * [factory] to instantiate the [ViewModel].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
fun <D> viewModelProvider(
    fragment: Fragment,
    scope: KClass<*>,
    factory: (D, SavedStateHandle) -> ViewModel
): ViewModelProvider {
    val context = fragment.requireContext()
    val viewModelFactory = SimpleSavedStateViewModelFactory(fragment, context, scope, factory)
    return ViewModelProvider(fragment, viewModelFactory)
}

@InternalWhetstoneApi
@Composable
fun <D> rememberViewModelProvider(
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

private class SimpleSavedStateViewModelFactory<D>(
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
