package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import com.freeletics.mad.navigator.BaseRoute
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel] for the given [entry]. The `ViewModel.Factory` will use [scope] to lookup
 * a parent component instance. That component will then be passed to the given [factory] together
 * with a [SavedStateHandle] and the passed in [route].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
public inline fun <reified T : ViewModel, D : Any, R : BaseRoute> viewModel(
    entry: NavBackStackEntry,
    context: Context,
    scope: KClass<*>,
    destinationScope: KClass<*>,
    route: R,
    noinline findEntry: (Int) -> NavBackStackEntry,
    crossinline factory: (D, SavedStateHandle, R) -> T
): T {
    val viewModelFactory = viewModelFactory {
        initializer {
            val dependencies = context.findComponentByScope<D>(scope, destinationScope, findEntry)
            val savedStateHandle = createSavedStateHandle()
            factory(dependencies, savedStateHandle, route)
        }
    }
    val viewModelProvider = ViewModelProvider(entry, viewModelFactory)
    return viewModelProvider[T::class.java]
}
