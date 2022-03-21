package com.freeletics.mad.whetstone.compose.internal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
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
import com.freeletics.mad.whetstone.internal.findDependencies
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
public inline fun <reified T : ViewModel, D, E> rememberViewModel(
    scope: KClass<*>,
    extra: E,
    crossinline factory: @DisallowComposableCalls (D, SavedStateHandle, E) -> T
): T {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val context = LocalContext.current
    return remember(viewModelStoreOwner, savedStateRegistryOwner, context, extra) {
        val viewModelFactory = WhetstoneViewModelFactory(savedStateRegistryOwner) {
            val dependencies = context.findDependencies<D>(scope::class)
            factory(dependencies, it, extra)
        }
        val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
        viewModelProvider[T::class.java]
    }
}
