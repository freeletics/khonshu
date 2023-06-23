package com.freeletics.khonshu.codegen.internal

import android.content.Context
import com.freeletics.khonshu.codegen.NavEntryComponent
import com.freeletics.khonshu.navigation.internal.InternalNavigationApi
import com.freeletics.khonshu.navigation.internal.NavigationExecutor
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A generated implementation of this can be used to retrieve a generated [NavEntryComponent].
 */
@InternalCodegenApi
public interface NavEntryComponentGetter {
    /**
     * The implementation should return an instance of the generated nav entry component.
     */
    @OptIn(InternalNavigationApi::class)
    public fun retrieve(executor: NavigationExecutor, context: Context): Any
}

/**
 * Used when binding a [NavEntryComponentGetter] into a map using the scope class as key.
 *
 * To be used in generated code.
 */
@MapKey
@InternalCodegenApi
public annotation class NavEntryComponentGetterKey(val value: KClass<*>)
