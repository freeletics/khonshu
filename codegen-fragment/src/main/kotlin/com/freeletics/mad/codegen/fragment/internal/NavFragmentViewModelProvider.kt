package com.freeletics.mad.codegen.fragment.internal

import androidx.compose.runtime.DisallowComposableCalls
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.freeletics.mad.codegen.internal.InternalCodegenApi
import com.freeletics.mad.codegen.internal.findComponentByScope
import com.freeletics.mad.navigation.BaseRoute
import com.freeletics.mad.navigation.fragment.findNavigationExecutor
import com.freeletics.mad.navigation.internal.destinationId
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel]. The `ViewModel.Factory` will use [parentScope] to lookup
 * a parent component instance. That component will then be passed to the given [factory] together
 * with a [SavedStateHandle] and the passed in [route].
 *
 * To be used in generated code.
 */
@InternalCodegenApi
public inline fun <reified C : Any, P : Any, R : BaseRoute> Fragment.component(
    parentScope: KClass<*>,
    destinationScope: KClass<*>,
    route: R,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle, R) -> C,
): C {
    val executor = findNavigationExecutor()
    return executor.storeFor(route.destinationId).getOrCreate(C::class) {
        val parentComponent = requireContext().findComponentByScope<P>(parentScope, destinationScope, executor)
        val savedStateHandle = executor.savedStateHandleFor(route.destinationId)
        factory(parentComponent, savedStateHandle, route)
    }
}
