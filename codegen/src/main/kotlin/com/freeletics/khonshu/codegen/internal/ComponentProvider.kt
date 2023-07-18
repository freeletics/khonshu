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

/**
 * Creates a [ViewModel] for the given [destinationId]. The `ViewModel.Factory` will use [parentScope]
 * to lookup a parent component instance. That component will then be passed to the given [factory]
 * together with a [SavedStateHandle] and the passed in [destination].
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
    destinationScope: KClass<*>,
    crossinline factory: (P, SavedStateHandle, R) -> C,
): C {
    return executor.storeFor(destinationId).getOrCreate(C::class) {
        val component = context.findComponentByScope<P>(parentScope, destinationScope, executor)
        val savedStateHandle = executor.savedStateHandleFor(destinationId)
        factory(component, savedStateHandle, route)
    }
}

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
    val route = executor.routeFor(DestinationId(destination))
    return component(
        destinationId = destinationId,
        route = route,
        executor = executor,
        context = context,
        parentScope = parentScope,
        destinationScope = destinationScope,
        factory = factory,
    )
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
internal fun <T : Any> Context.findComponentByScope(
    scope: KClass<*>,
    destinationScope: KClass<*>,
    executor: NavigationExecutor,
): T {
    if (scope != destinationScope) {
        val destinationComponent = find(destinationScope) as? NavDestinationComponent
        val getter = destinationComponent?.navEntryComponentGetters?.get(scope.java)
        if (getter != null) {
            @Suppress("UNCHECKED_CAST")
            return getter.retrieve(executor, this) as T
        }
    }
    return findComponentByScope(scope)
}
