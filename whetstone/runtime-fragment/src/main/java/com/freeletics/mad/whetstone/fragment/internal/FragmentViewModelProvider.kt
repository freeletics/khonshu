package com.freeletics.mad.whetstone.fragment.internal

import android.content.Context
import android.os.Bundle
import android.view.View
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
 * Creates a [ViewModelProvider] for the given [fragment] that uses a custom
 * [AbstractSavedStateViewModelFactory]. That factory will use [scope] to lookup the required
 * dependencies interface [D] implementation from either the `Activity` or the `Application`
 * `Context`. The [D] instance will then, together with the [SavedStateHandle] be passed to
 * [factory] to instantiate the [ViewModel].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
public inline fun <reified T : ViewModel, D, E> Fragment.viewModel(
    scope: KClass<*>,
    extra: E,
    crossinline factory: @DisallowComposableCalls (D, SavedStateHandle, E) -> T
): T {
    val viewModelFactory = WhetstoneViewModelFactory(this) {
        val dependencies = requireContext().findDependencies<D>(scope)
        factory(dependencies, it, extra)
    }
    val viewModelProvider = ViewModelProvider(this, viewModelFactory)
    return viewModelProvider[T::class.java]
}
