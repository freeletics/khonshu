package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.navigation.NavBackStackEntry
import com.freeletics.mad.whetstone.NavEntryComponent
import com.freeletics.mad.whetstone.NavEntryId
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A generated implementation of this can be used to retrieve a generated [NavEntryComponent].
 *
 * The implementation will be bound into a `Map<String, NavEntryComponentGetter>` were the key
 * is the same scope that is used in [NavEntryId] and [NavEntryComponent].
 */
@InternalWhetstoneApi
public interface NavEntryComponentGetter {
    /**
     * The id that is passed as parameter to [findEntry] is the id that was provided with
     * [NavEntryId]. The given [findEntry] should look up a back strack entry for that id
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
