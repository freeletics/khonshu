package com.freeletics.mad.whetstone.compose.internal

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freeletics.mad.whetstone.internal.InternalWhetstoneApi
import com.freeletics.mad.whetstone.internal.findComponentByScope
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
    val context = LocalContext.current
    return remember(viewModelStoreOwner, context, arguments) {
        val viewModelFactory = viewModelFactory {
            initializer {
                val dependencies = context.findComponentByScope<D>(scope)
                val savedStateHandle = createSavedStateHandle()
                factory(dependencies, savedStateHandle, arguments)
            }
        }
        val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
        viewModelProvider[T::class.java]
    }
}
