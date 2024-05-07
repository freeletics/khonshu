package com.freeletics.khonshu.navigation.test

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.freeletics.khonshu.navigation.internal.MultiStack
import com.freeletics.khonshu.navigation.internal.MultiStackNavigationExecutor
import com.freeletics.khonshu.navigation.internal.Stack
import com.freeletics.khonshu.navigation.internal.StackEntry
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// TODO rename
internal fun Stack.computeVisibleEntries(): ImmutableList<StackEntry<*>> {
    return snapshot(id).visibleEntries()
}

// TODO remove state wrapper
internal val MultiStack.visibleEntries: State<ImmutableList<StackEntry<*>>>
    get() = mutableStateOf(snapshot.value.visibleEntries())

// TODO remove state wrapper
internal val MultiStack.canNavigateBack: State<Boolean>
    get() = mutableStateOf(snapshot.value.canNavigateBack)

// TODO remove state wrapper
internal val MultiStackNavigationExecutor.visibleEntries: MutableState<ImmutableList<StackEntry<*>>>
    get() = mutableStateOf(snapshot.value.visibleEntries())

// TODO remove state wrapper
internal val MultiStackNavigationExecutor.canNavigateBack: State<Boolean>
    get() = mutableStateOf(snapshot.value.canNavigateBack)

internal fun StackSnapshot.visibleEntries(): ImmutableList<StackEntry<*>> {
    val entries = mutableListOf<StackEntry<*>>()
    forEachVisibleDestination { entries.add(it) }
    return entries.toImmutableList()
}
