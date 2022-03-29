package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.navigation.NavBackStackEntry
import kotlin.reflect.KClass

/**
 * Looks up [T] by searching for a fitting [NavEntryComponentGetter] or finding the [scope] in
 * [Context.getSystemService].
 */
@InternalWhetstoneApi
public fun <T> Context.findDependencies(
    scope: KClass<*>,
    destinationScope: KClass<*>,
    findEntry: (Int) -> NavBackStackEntry
): T {
    val destinationComponent = find<DestinationComponent>(destinationScope)
    val getter = destinationComponent?.navEntryComponentGetters?.get(scope.java)
    if (getter != null) {
        @Suppress("UNCHECKED_CAST")
        return getter.retrieve(findEntry, this) as T
    }
    return find(scope)!!
}
