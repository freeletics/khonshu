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
public interface GraphProvider<R : BaseRoute, T> {
    public fun provide(entry: StackEntry<R>, snapshot: StackSnapshot, provider: ActivityGraphProvider): T
}

/**
 * Creates a [ViewModel] for the given [destinationId]. The `ViewModel.Factory` will use [parentScope]
 * to lookup a parent graph instance. That graph will then be passed to the given [factory]
 * together with a [SavedStateHandle] and the passed in [destinationId].
 *
 * To be used in generated code.
 */
@InternalCodegenApi
public inline fun <reified C : Any, PC : Any, R : BaseRoute> getGraph(
    entry: StackEntry<R>,
    activityGraphProvider: ActivityGraphProvider,
    parentScope: KClass<*>,
    crossinline factory: (PC) -> C,
): C {
    return entry.store.getOrCreate(C::class) {
        val parentGraph = activityGraphProvider.provide<PC>(parentScope)
        factory(parentGraph)
    }
}

/**
 * Creates a [ViewModel] for the given [destinationId]. The `ViewModel.Factory` will use [parentScope]
 * to lookup a parent graph instance. That graph will then be passed to the given [factory]
 * together with a [SavedStateHandle] and the passed in [destinationId].
 *
 * To be used in generated code.
 */
@InternalCodegenApi
public inline fun <reified C : Any, PC : Any, R : BaseRoute, PR : BaseRoute> getGraphFromParentRoute(
    entry: StackEntry<R>,
    snapshot: StackSnapshot,
    activityGraphProvider: ActivityGraphProvider,
    parentScope: KClass<PR>,
    crossinline factory: (PC) -> C,
): C {
    return entry.store.getOrCreate(C::class) {
        val parentEntry = snapshot.entryFor(DestinationId(parentScope))

        @Suppress("UNCHECKED_CAST")
        val parentGraphProvider = parentEntry.extra as GraphProvider<PR, PC>
        val parentGraph = parentGraphProvider.provide(parentEntry, snapshot, activityGraphProvider)
        factory(parentGraph)
    }
}
