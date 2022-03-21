package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.navigation.NavBackStackEntry
import com.freeletics.mad.whetstone.NavEntryComponent
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A generated implementation of this can be used to retrieve a generated [NavEntryComponent].
 *
 * The implementation will be bound into a `Map<Class<*>, NavEntryComponentGetter>` were the key
 * is the same scope that is used in [NavEntryComponent]. It can be used through
 * [com.freeletics.mad.whetstone.NavEntryComponents].
 */
@InternalWhetstoneApi
public interface NavEntryComponentGetter {
    /**
     * The given [findEntry] should look up a back strack entry for that id
     * in the current `NavController`.
     */
    public fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any
}

/**
 * Used when binding a [NavEntryComponentGetter] into a map. The value is the fully qualified name
 * of the scope class.
 *
 * The map can be used to easily implement an [android.content.Context.getSystemService] override
 * where a scope was passed into to retrieve the component for that scope.
 *
 * To be used in generated code.
 */
@MapKey
@InternalWhetstoneApi
public annotation class NavEntryComponentGetterKey(val value: KClass<*>)
