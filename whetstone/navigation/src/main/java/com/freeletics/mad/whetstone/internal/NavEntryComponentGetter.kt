package com.freeletics.mad.whetstone.internal

import android.content.Context
import com.freeletics.mad.navigator.internal.InternalNavigatorApi
import com.freeletics.mad.navigator.internal.NavigationExecutor
import com.freeletics.mad.whetstone.NavEntryComponent
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A generated implementation of this can be used to retrieve a generated [NavEntryComponent].
 */
@InternalWhetstoneApi
public interface NavEntryComponentGetter {
    /**
     * The implementation should return an instance of the generated nav entry component.
     */
    @OptIn(InternalNavigatorApi::class)
    public fun retrieve(executor: NavigationExecutor, context: Context): Any
}

/**
 * Used when binding a [NavEntryComponentGetter] into a map using the scope class as key.
 *
 * To be used in generated code.
 */
@MapKey
@InternalWhetstoneApi
public annotation class NavEntryComponentGetterKey(val value: KClass<*>)
