package com.freeletics.khonshu.codegen.internal

import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.internal.DestinationId
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import com.freeletics.khonshu.navigation.internal.destinationId
import kotlin.reflect.KClass

@InternalCodegenApi
public interface DestinationGraphProvider<R : BaseRoute, ParentGraph, Graph> {
    public fun getParentGraph(snapshot: StackSnapshot, provider: HostGraphProvider): ParentGraph
    // hostGraphProvider.provide<ParentGraph>(ParentScope::class)

    // val parentEntry = snapshot.entryFor(DestinationId(ParentScope::class))
    // @Suppress("UNCHECKED_CAST")
    // val parentGraphProvider = parentEntry.extra as DestinationGraphProvider<ParentScope, *, ParentGraph>
    // parentDestinationGraphProvider.provide(parentEntry, snapshot, hostGraphProvider)

    public fun provide(entry: StackEntry<R>, snapshot: StackSnapshot, provider: HostGraphProvider): Graph
    // entry.store.getOrCreate(Graph::class) {
    //   getParentGraph(snapshot, provider).create(entry.savedStateHandle, entry.route)
    // }
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
    hostGraphProvider: HostGraphProvider,
    parentScope: KClass<*>,
    crossinline factory: (PC) -> C,
): C {
    return entry.store.getOrCreate(C::class) {
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
    hostGraphProvider: HostGraphProvider,
    parentScope: KClass<PR>,
    crossinline factory: (PC) -> C,
): C {
    return entry.store.getOrCreate(C::class) {
        val parentEntry = snapshot.entryFor(DestinationId(parentScope))

        @Suppress("UNCHECKED_CAST")
        val parentDestinationGraphProvider = parentEntry.extra as DestinationGraphProvider<PR, PC>
        val parentGraph = parentDestinationGraphProvider.provide(parentEntry, snapshot, hostGraphProvider)
        factory(parentGraph)
    }
}
