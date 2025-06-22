package com.freeletics.khonshu.codegen.internal

import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.freeletics.khonshu.codegen.GlobalGraphProvider
import com.freeletics.khonshu.navigation.internal.StackEntryStore
import kotlin.reflect.KClass

@InternalCodegenApi
public interface HostGraphProvider {
    public fun <T> provide(scope: KClass<*>): T
}

@InternalCodegenApi
public val LocalHostGraphProvider: ProvidableCompositionLocal<HostGraphProvider> =
    staticCompositionLocalOf {
        throw IllegalStateException("HostGraphProvider was not provided")
    }

@InternalCodegenApi
public inline fun <C : Any, reified AC : Any, P : Any> getGraph(
    store: StackEntryStore,
    globalGraphProvider: GlobalGraphProvider,
    requestedScope: KClass<*>,
    activityScope: KClass<*>,
    activityParentScope: KClass<*>,
    crossinline factory: @DisallowComposableCalls (P) -> AC,
): C {
    if (requestedScope != activityScope) {
        return globalGraphProvider.getGraph(requestedScope)
    }
    @Suppress("UNCHECKED_CAST")
    return store.getOrCreate(AC::class) {
        val parentGraph = globalGraphProvider.getGraph<P>(activityParentScope)
        factory(parentGraph)
    } as C
}
