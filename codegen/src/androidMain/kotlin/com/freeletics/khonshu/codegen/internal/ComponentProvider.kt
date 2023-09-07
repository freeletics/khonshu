package com.freeletics.khonshu.codegen.internal

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import java.io.Serializable
import kotlin.reflect.KClass

@InternalCodegenApi
public inline fun <reified C : Any, P : Any> component(
    viewModelStoreOwner: ViewModelStoreOwner,
    context: Context,
    parentScope: KClass<*>,
    arguments: Bundle?,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle, Bundle) -> C,
): C {
    val store = ViewModelProvider(viewModelStoreOwner, SavedStateViewModelFactory())[StoreViewModel::class.java]
    return store.getOrCreate(C::class) {
        val parentComponent = context.findComponentByScope<P>(parentScope)
        val savedStateHandle = store.savedStateHandle
        factory(parentComponent, savedStateHandle, arguments ?: Bundle.EMPTY)
    }
}

public interface ComponentProvider<R : BaseRoute, T> : Serializable {
    public fun provide(route: R, executor: NavigationExecutor, context: Context): T
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
    destinationId: DestinationId<out R>,
    route: R,
    executor: NavigationExecutor,
    context: Context,
    parentScope: KClass<*>,
    crossinline factory: (PC, SavedStateHandle, R) -> C,
): C {
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val parentComponent = context.findComponentByScope<PC>(parentScope)
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
    destinationId: DestinationId<out R>,
    route: R,
    executor: NavigationExecutor,
    context: Context,
    parentScope: KClass<PR>,
    crossinline factory: (PC, SavedStateHandle, R) -> C,
): C {
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val parentDestinationId = DestinationId((parentScope))

        @Suppress("UNCHECKED_CAST")
        val parentComponentProvider = executor.extra(parentDestinationId) as ComponentProvider<PR, PC>
        val parentRoute = executor.routeFor(parentDestinationId)
        val parentComponent = parentComponentProvider.provide(parentRoute, executor, context)
        val savedStateHandle = executor.savedStateHandleFor(destinationId)
        factory(parentComponent, savedStateHandle, route)
    }
}

@PublishedApi
internal fun <T> Context.findComponentByScope(scope: KClass<*>): T {
    val serviceName = scope.qualifiedName!!
    val component = getSystemService(serviceName) ?: applicationContext.getSystemService(serviceName)
    checkNotNull(component) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return component as T
}
