package com.freeletics.mad.whetstone.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.freeletics.mad.whetstone.internal.InternalWhetstoneApi
import com.freeletics.mad.whetstone.internal.WhetstoneViewModelFactory
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
        val viewModelFactory = WhetstoneViewModelFactory(savedStateRegistryOwner, context, scope, factory)
        ViewModelProvider(viewModelStoreOwner, viewModelFactory)
    }
}
