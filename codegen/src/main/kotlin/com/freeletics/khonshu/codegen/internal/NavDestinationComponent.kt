package com.freeletics.khonshu.codegen.internal

import dagger.MapKey
import kotlin.reflect.KClass

@InternalCodegenApi
public interface NavDestinationComponent {
    public val navEntryComponentGetters: @JvmSuppressWildcards Map<Class<*>, NavEntryComponentGetter>
    public val componentProviders: @JvmSuppressWildcards Map<Class<*>, ComponentProvider<*, *>>
}

/**
 * Used when binding a [ComponentProvider] into a map using the scope class as key.
 *
 * To be used in generated code.
 */
@MapKey
@InternalCodegenApi
public annotation class NavComponentProvider(val value: KClass<*>)
