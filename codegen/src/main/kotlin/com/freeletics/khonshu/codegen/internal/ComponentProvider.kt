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
import kotlin.reflect.KClass

@InternalCodegenApi
public inline fun <reified C : Any, P : Any> component(
    viewModelStoreOwner: ViewModelStoreOwner,
    context: Context,
    parentScope: KClass<*>,
    arguments: Bundle,
    crossinline factory: @DisallowComposableCalls (P, SavedStateHandle, Bundle) -> C,
): C {
    val store = ViewModelProvider(viewModelStoreOwner, SavedStateViewModelFactory())[StoreViewModel::class.java]
    return store.getOrCreate(C::class) {
        val parentComponent = context.findComponentByScope<P>(parentScope)
        val savedStateHandle = store.savedStateHandle
        factory(parentComponent, savedStateHandle, arguments)
    }
}

public interface ComponentProvider<R : BaseRoute, T> {
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
public inline fun <reified C : Any, P : Any, R : BaseRoute> component(
    destinationId: DestinationId<out R>,
    route: R,
    executor: NavigationExecutor,
    context: Context,
    parentScope: KClass<*>,
    crossinline factory: (P, SavedStateHandle, R) -> C,
): C {
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val component = context.findComponentByScope<P>(parentScope)
        val savedStateHandle = executor.savedStateHandleFor(destinationId)
        factory(component, savedStateHandle, route)
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
public inline fun <reified C : Any, P : Any, PR : BaseRoute, R : BaseRoute> componentFromParentRoute(
    destinationId: DestinationId<out R>,
    route: R,
    executor: NavigationExecutor,
    context: Context,
    parentScope: KClass<PR>,
    destinationScope: KClass<*>,
    crossinline factory: (P, SavedStateHandle, R) -> C,
): C {
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val component = context.findComponentByScope<P, PR>(parentScope, destinationScope, executor)
        val savedStateHandle = executor.savedStateHandleFor(destinationId)
        factory(component, savedStateHandle, route)
    }
}

@PublishedApi
internal fun <T> Context.findComponentByScope(scope: KClass<*>): T {
    val component = find(scope)
    checkNotNull(component) {
        "Could not find scope ${scope.qualifiedName} through getSystemService"
    }
    @Suppress("UNCHECKED_CAST")
    return component as T
}

private fun Context.find(service: KClass<*>): Any? {
    val serviceName = service.qualifiedName!!
    return getSystemService(serviceName) ?: applicationContext.getSystemService(serviceName)
}

/**
 * Looks up [T] by searching for a fitting [NavEntryComponentGetter] or finding the [scope] in
 * [Context.getSystemService].
 */
@PublishedApi
internal fun <T : Any, R : BaseRoute> Context.findComponentByScope(
    scope: KClass<R>,
    destinationScope: KClass<*>,
    executor: NavigationExecutor,
): T {
    if (scope != destinationScope) {
        val destinationComponent = find(destinationScope) as? NavDestinationComponent

        @Suppress("UNCHECKED_CAST")
        val provider = destinationComponent?.componentProviders?.get(scope.java) as ComponentProvider<R, T>?
        if (provider != null) {
            val route = executor.routeFor(DestinationId(scope))
            return provider.provide(route, executor, this)
        }
    }
    return findComponentByScope(scope)
}
