package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal val StackSnapshot.visibleEntries: ImmutableList<StackEntry<*>>
    get() {
        val entries = mutableListOf<StackEntry<*>>()
        forEachVisibleDestination { entries.add(it) }
        return entries.toImmutableList()
    }
