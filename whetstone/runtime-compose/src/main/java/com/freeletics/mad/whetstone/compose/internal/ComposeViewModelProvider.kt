package com.freeletics.mad.whetstone.compose.internal

import android.os.Bundle
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
 * Creates a [ViewModel]. The `ViewModel.Factory` will use [scope] to lookup
 * a parent component instance. That component will then be passed to the given [factory] together
 * with a [SavedStateHandle] and the passed in [arguments].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
@Composable
public inline fun <reified T : ViewModel, D : Any> rememberViewModel(
    scope: KClass<*>,
    arguments: Bundle,
    crossinline factory: @DisallowComposableCalls (D, SavedStateHandle, Bundle) -> T
): T {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val context = LocalContext.current
    return remember(viewModelStoreOwner, savedStateRegistryOwner, context, arguments) {
        val viewModelFactory = WhetstoneViewModelFactory(savedStateRegistryOwner) {
            val dependencies = context.findDependencies<D>(scope::class)
            factory(dependencies, it, arguments)
        }
        val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
        viewModelProvider[T::class.java]
    }
}
