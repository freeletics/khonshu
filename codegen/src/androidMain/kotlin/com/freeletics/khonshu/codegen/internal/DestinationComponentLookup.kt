package com.freeletics.khonshu.codegen.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import com.freeletics.khonshu.navigation.internal.destinationId
import kotlin.reflect.KClass

@InternalCodegenApi
public interface ComponentProvider<R : BaseRoute, T> {
    public fun provide(entry: StackEntry<R>, snapshot: StackSnapshot, provider: ActivityComponentProvider): T
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
    entry: StackEntry<R>,
    activityComponentProvider: ActivityComponentProvider,
    parentScope: KClass<*>,
    crossinline factory: (PC) -> C,
): C {
    return entry.store.getOrCreate(C::class) {
        val parentComponent = activityComponentProvider.provide<PC>(parentScope)
        factory(parentComponent)
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
    entry: StackEntry<R>,
    snapshot: StackSnapshot,
    activityComponentProvider: ActivityComponentProvider,
    parentScope: KClass<PR>,
    crossinline factory: (PC) -> C,
): C {
    return entry.store.getOrCreate(C::class) {
        val parentEntry = snapshot.entryFor(DestinationId(parentScope))

        @Suppress("UNCHECKED_CAST")
        val parentComponentProvider = parentEntry.extra as ComponentProvider<PR, PC>
        val parentComponent = parentComponentProvider.provide(parentEntry, snapshot, activityComponentProvider)
        factory(parentComponent)
    }
}
