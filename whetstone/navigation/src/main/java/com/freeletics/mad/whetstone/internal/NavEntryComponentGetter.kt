package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.navigation.NavBackStackEntry
import com.freeletics.mad.whetstone.NavEntryComponent
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A generated implementation of this can be used to retrieve a generated [NavEntryComponent].
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
 * Used when binding a [NavEntryComponentGetter] into a map using the scope class as key.
 *
 * To be used in generated code.
 */
@MapKey
@InternalWhetstoneApi
public annotation class NavEntryComponentGetterKey(val value: KClass<*>)
