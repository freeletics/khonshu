package com.freeletics.mad.whetstone

import android.content.Context
import androidx.navigation.NavBackStackEntry

/**
 * A generated implementation of this can be used to retrieve a generated [NavEntryComponent].
 *
 * The implementation will be bound into a `Map<String, NavEntryComponentGetter` were the key
 * is the same scope that is used in [NavEntryId] and [NavEntryComponent].
 */
interface NavEntryComponentGetter {
    /**
     * The id that is passed as parameter to [findEntry] is the id that was provided with
     * [NavEntryId]. The given [findEntry] should look up a back strack entry for that id
     * in the current `NavController`.
     */
    fun retrieve(findEntry: (Int) -> NavBackStackEntry, context: Context): Any
}
