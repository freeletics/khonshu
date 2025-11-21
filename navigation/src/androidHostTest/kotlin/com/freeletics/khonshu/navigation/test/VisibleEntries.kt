package com.freeletics.khonshu.navigation.test

import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot

internal val StackSnapshot.visibleEntries: List<StackEntry<*>>
    get() {
        val entries = mutableListOf<StackEntry<*>>()
        forEachVisibleDestination { entries.add(it) }
        return entries
    }
