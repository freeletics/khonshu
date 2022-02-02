package com.freeletics.mad.whetstone.fragment.internal

import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.freeletics.mad.whetstone.internal.InternalWhetstoneApi
import com.freeletics.mad.whetstone.internal.SimpleSavedStateViewModelFactory
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
public fun <D> viewModelProvider(
    fragment: Fragment,
    scope: KClass<*>,
    factory: (D, SavedStateHandle) -> ViewModel
): ViewModelProvider {
    val context = fragment.requireContext()
    val viewModelFactory = SimpleSavedStateViewModelFactory(fragment, context, scope, factory)
    return ViewModelProvider(fragment, viewModelFactory)
}
