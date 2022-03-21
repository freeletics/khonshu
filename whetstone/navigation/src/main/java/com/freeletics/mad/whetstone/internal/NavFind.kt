package com.freeletics.mad.whetstone.internal

import android.content.Context
import androidx.navigation.NavBackStackEntry
import com.freeletics.mad.whetstone.NavEntryComponents
import kotlin.reflect.KClass

/**
 * Looks up [T] by searching for a fitting [NavEntryComponentGetter] or finding the [scope] in
 * [Context.getSystemService].
 */
@InternalWhetstoneApi
public fun <T> Context.findDependencies(scope: KClass<*>, findEntry: (Int) -> NavBackStackEntry): T {
    val components = find<NavEntryComponents>(NavEntryComponents::class)
    return components?.get(scope, this, findEntry) ?: find(scope)!!
}
