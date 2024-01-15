package com.freeletics.khonshu.codegen.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import com.freeletics.khonshu.navigation.internal.destinationId
import java.io.Serializable
import kotlin.reflect.KClass

@InternalCodegenApi
public interface ComponentProvider<R : BaseRoute, T> : Serializable {
    public fun provide(route: R, executor: NavigationExecutor, provider: ActivityComponentProvider): T
}

/**
 * Creates a [ViewModel] for the given [destinationId]. The `ViewModel.Factory` will use [parentScope]
 * to lookup a parent component instance. That component will then be passed to the given [factory]
 * together with a [SavedStateHandle] and the passed in [destinationId].
 *
 * To be used in generated code.
 */
@InternalCodegenApi
public inline fun <reified C : Any, PC : Any, R : BaseRoute> component(
    route: R,
    executor: NavigationExecutor,
    activityComponentProvider: ActivityComponentProvider,
    parentScope: KClass<*>,
    crossinline factory: (PC, SavedStateHandle, R) -> C,
): C {
    val destinationId = route.destinationId
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val parentComponent = activityComponentProvider.provide<PC>(parentScope)
        val savedStateHandle = executor.savedStateHandleFor(destinationId)
        factory(parentComponent, savedStateHandle, route)
    }
}

/**
 * Creates a [ViewModel] for the given [destinationId]. The `ViewModel.Factory` will use [parentScope]
 * to lookup a parent component instance. That component will then be passed to the given [factory]
 * together with a [SavedStateHandle] and the passed in [destinationId].
 *
 * To be used in generated code.
 */
@InternalCodegenApi
public inline fun <reified C : Any, PC : Any, R : BaseRoute, PR : BaseRoute> componentFromParentRoute(
    route: R,
    executor: NavigationExecutor,
    activityComponentProvider: ActivityComponentProvider,
    parentScope: KClass<PR>,
    crossinline factory: (PC, SavedStateHandle, R) -> C,
): C {
    val destinationId = route.destinationId
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val parentDestinationId = DestinationId((parentScope))

        @Suppress("UNCHECKED_CAST")
        val parentComponentProvider = executor.extra(parentDestinationId) as ComponentProvider<PR, PC>
        val parentRoute = executor.routeFor(parentDestinationId)
        val parentComponent = parentComponentProvider.provide(parentRoute, executor, activityComponentProvider)
        val savedStateHandle = executor.savedStateHandleFor(destinationId)
        factory(parentComponent, savedStateHandle, route)
    }
}
