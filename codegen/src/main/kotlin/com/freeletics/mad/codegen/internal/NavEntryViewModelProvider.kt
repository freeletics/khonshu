package com.freeletics.mad.codegen.internal

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.freeletics.mad.navigation.BaseRoute
import com.freeletics.mad.navigation.internal.DestinationId
import com.freeletics.mad.navigation.internal.NavigationExecutor
import kotlin.reflect.KClass

/**
 * Creates a [ViewModel] for the given [destination]. The `ViewModel.Factory` will use [parentScope]
 * to lookup a parent component instance. That component will then be passed to the given [factory]
 * together with a [SavedStateHandle] and the passed in [destination].
 *
 * To be used in generated code.
 */
@InternalCodegenApi
public inline fun <reified C : Any, P : Any, R : BaseRoute> navEntryComponent(
    destination: KClass<R>,
    executor: NavigationExecutor,
    context: Context,
    parentScope: KClass<*>,
    destinationScope: KClass<*>,
    crossinline factory: (P, SavedStateHandle, R) -> C,
): C {
    val destinationId = DestinationId(destination)
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val component = context.findComponentByScope<P>(parentScope, destinationScope, executor)
        val savedStateHandle = executor.savedStateHandleFor(destinationId)
        val route = executor.routeFor(destinationId)
        factory(component, savedStateHandle, route)
    }
}
