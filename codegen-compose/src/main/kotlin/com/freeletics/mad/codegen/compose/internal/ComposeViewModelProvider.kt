package com.freeletics.mad.codegen.compose.internal

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.freeletics.mad.codegen.internal.InternalCodegenApi
import com.freeletics.mad.codegen.internal.StoreViewModel
import com.freeletics.mad.codegen.internal.findComponentByScope
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel]. The `ViewModel.Factory` will use [parentScope] to lookup
 * a parent component instance. That component will then be passed to the given [factory] together
 * with a [SavedStateHandle] and the passed in [arguments].
 *
 * To be used in generated code.
 */
@InternalCodegenApi
@Composable
public inline fun <reified C : Any, P : Any> rememberComponent(
    parentScope: KClass<*>,
    arguments: Bundle,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle, Bundle) -> C,
): C {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val context = LocalContext.current
    return remember(viewModelStoreOwner, context, arguments) {
        val store = ViewModelProvider(viewModelStoreOwner, SavedStateViewModelFactory())[StoreViewModel::class.java]
        store.getOrCreate(C::class) {
            val parentComponent = context.findComponentByScope<P>(parentScope)
            val savedStateHandle = store.savedStateHandle
            factory(parentComponent, savedStateHandle, arguments)
        }
    }
}
