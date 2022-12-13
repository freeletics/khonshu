package com.freeletics.mad.whetstone.fragment.internal

import androidx.compose.runtime.DisallowComposableCalls
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.fragment.findNavigationExecutor
import com.freeletics.mad.navigator.internal.DestinationId
import com.freeletics.mad.navigator.internal.destinationId
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
public inline fun <reified T : ViewModel, D : Any, R : BaseRoute> Fragment.viewModel(
    scope: KClass<*>,
    destinationScope: KClass<*>,
    route: R,
    crossinline factory: @DisallowComposableCalls (D, SavedStateHandle, R) -> T
): T {
    val viewModelFactory = viewModelFactory {
        initializer {
            val executor = findNavigationExecutor()
            val dependencies = requireContext().findComponentByScope<D>(scope, destinationScope, executor)
            val savedStateHandle = executor.savedStateHandleFor(route.destinationId)
            factory(dependencies, savedStateHandle, route)
        }
    }
    val viewModelProvider = ViewModelProvider(this, viewModelFactory)
    return viewModelProvider[T::class.java]
}
