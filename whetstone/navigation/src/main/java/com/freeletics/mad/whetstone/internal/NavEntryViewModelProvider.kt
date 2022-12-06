package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.internal.NavigationExecutor
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel] for the given [destination]. The `ViewModel.Factory` will use [parentScope]
 * to lookup a parent component instance. That component will then be passed to the given [factory]
 * together with a [SavedStateHandle] and the passed in [destination].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
public inline fun <reified T : ViewModel, D : Any, R : BaseRoute> navEntryViewModel(
    destination: KClass<R>,
    executor: NavigationExecutor,
    context: Context,
    parentScope: KClass<*>,
    destinationScope: KClass<*>,
    crossinline factory: (D, SavedStateHandle, R) -> T
): T {
    val viewModelFactory = viewModelFactory {
        initializer {
            val component = context.findComponentByScope<D>(parentScope, destinationScope, executor)
            val savedStateHandle = createSavedStateHandle()
            val route = executor.routeFor(destination)
            factory(component, savedStateHandle, route)
        }
    }
    val viewModelStore = executor.viewModelStoreFor(destination)
    val viewModelProvider = ViewModelProvider(viewModelStore, viewModelFactory)
    return viewModelProvider[T::class.java]
}
