package com.freeletics.mad.whetstone.fragment.internal

import android.os.Bundle
import androidx.compose.runtime.DisallowComposableCalls
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import com.freeletics.mad.whetstone.internal.InternalWhetstoneApi
import com.freeletics.mad.whetstone.internal.StoreViewModel
import com.freeletics.mad.whetstone.internal.findComponentByScope
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel]. The `ViewModel.Factory` will use [parentScope] to lookup
 * a parent component instance. That component will then be passed to the given [factory] together
 * with a [SavedStateHandle] and the passed in [arguments].
 *
 * To be used in generated code.
 */
@InternalWhetstoneApi
public inline fun <reified C : Any, P : Any> Fragment.component(
    parentScope: KClass<*>,
    arguments: Bundle,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle, Bundle) -> C
): C {
    val store = ViewModelProvider(this, SavedStateViewModelFactory())[StoreViewModel::class.java]
    return store.getOrCreate(C::class) {
        val parentComponent = requireContext().findComponentByScope<P>(parentScope)
        val savedStateHandle = store.savedStateHandle
        factory(parentComponent, savedStateHandle, arguments)
    }
}
