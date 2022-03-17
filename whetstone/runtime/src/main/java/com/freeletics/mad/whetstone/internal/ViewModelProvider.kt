package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavBackStackEntry
import kotlin.reflect.KClass

/**
 * Creates a [ViewModelProvider] for the given [entry] that uses a custom
 * [AbstractSavedStateViewModelFactory]. That factory will use [scope] to lookup a parent
 * component [C] instance from either the `Activity` or the `Application`
 * `Context`. The [C] instance will then, together with the [SavedStateHandle] be passed to
 * [factory] to instantiate the [ViewModel].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
public fun <C> viewModelProvider(
    entry: NavBackStackEntry,
    context: Context,
    scope: KClass<*>,
    factory: (C, SavedStateHandle) -> ViewModel
): ViewModelProvider {
    val viewModelFactory = WhetstoneViewModelFactory(entry, context, scope, factory)
    return ViewModelProvider(entry, viewModelFactory)
}
