package com.freeletics.mad.whetstone.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.compose.LocalNavController
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.whetstone.internal.InternalWhetstoneApi
import com.freeletics.mad.whetstone.internal.findComponentByScope
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel]. The `ViewModel.Factory` will use [scope] to lookup
 * a parent component instance. That component will then be passed to the given [factory] together
 * with a [SavedStateHandle] and the passed in [route].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
@OptIn(InternalNavigatorApi::class)
@Composable
public inline fun <reified T : ViewModel, D : Any, R : BaseRoute> rememberViewModel(
    scope: KClass<*>,
    destinationScope: KClass<*>,
    route: R,
    crossinline factory: @DisallowComposableCalls (D, SavedStateHandle, R) -> T
): T {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val context = LocalContext.current
    val navController = LocalNavController.current
    return remember(viewModelStoreOwner, savedStateRegistryOwner, context, navController, route) {
        val viewModelFactory = viewModelFactory {
            initializer {
                val dependencies = context.findComponentByScope<D>(scope, destinationScope, navController::getBackStackEntry)
                val savedStateHandle = createSavedStateHandle()
                factory(dependencies, savedStateHandle, route)
            }
        }
        val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
        viewModelProvider[T::class.java]
    }
}
