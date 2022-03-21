package com.freeletics.mad.whetstone.fragment.internal

import android.os.Bundle
import androidx.compose.runtime.DisallowComposableCalls
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
public inline fun <reified T : ViewModel, D> Fragment.viewModel(
    scope: KClass<*>,
    arguments: Bundle,
    crossinline factory: @DisallowComposableCalls (D, SavedStateHandle, Bundle) -> T
): T {
    val viewModelFactory = WhetstoneViewModelFactory(this) {
        val dependencies = requireContext().findDependencies<D>(scope)
        factory(dependencies, it, arguments)
    }
    val viewModelProvider = ViewModelProvider(this, viewModelFactory)
    return viewModelProvider[T::class.java]
}
