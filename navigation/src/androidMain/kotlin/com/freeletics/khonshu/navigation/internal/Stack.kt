package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.saveable.Saver as ComposeSaver
import androidx.compose.runtime.saveable.SaverScope
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.SavedStateConfiguration
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute

internal class Stack private constructor(
    initialStack: List<StackEntry<*>>,
    private val createEntry: (BaseRoute) -> StackEntry<*>,
) {
    private val stack = ArrayDeque<StackEntry<*>>(20).also {
        it.addAll(initialStack)
    }

    val id: DestinationId<*> get() = rootEntry.destinationId

    @Suppress("UNCHECKED_CAST")
    val rootEntry: StackEntry<NavRoot> get() = stack.first() as StackEntry<NavRoot>

    val isAtRoot: Boolean get() = !stack.last().removable

    fun snapshot(startStackRootEntry: StackEntry<out NavRoot>): StackSnapshot {
        return StackSnapshot(stack.toList(), startStackRootEntry)
    }

    fun push(route: NavRoute) {
        val entry = createEntry(route)
        stack.add(entry)
        if (entry.parentDesintationId != null) {
            check(stack.any { it.destinationId == entry.parentDesintationId }) {
                "Navigated to $route with parent ${entry.parentDesintationId} but did not find the parent on " +
                    "the current stack: ${stack.joinToString(separator = ", ") { it.route.toString() }}"
            }
        }
    }

    fun pop() {
        check(stack.last().removable) { "Can't pop the root of the back stack" }
        popInternal()
    }

    private fun popInternal() {
        val entry = stack.removeLast()
        entry.close()
    }

    fun popUpTo(destinationId: DestinationId<*>, isInclusive: Boolean) {
        while (stack.last().destinationId != destinationId) {
            check(stack.last().removable) { "Route ${destinationId.route} not found on back stack" }
            popInternal()
        }

        if (isInclusive) {
            // using pop here to get the default removable check
            pop()
        }
    }

    fun clear() {
        while (stack.last().removable) {
            popInternal()
        }
    }

    companion object {
        fun createWith(
            root: NavRoot,
            createEntry: (BaseRoute) -> StackEntry<*>,
        ): Stack {
            val rootEntry = createEntry(root)
            return Stack(listOf(rootEntry), createEntry)
        }

        private const val KEY_ENTRIES = "entries"
    }

    class Saver(
        private val createEntry: (BaseRoute) -> StackEntry<*>,
        createRestoredEntry: (BaseRoute, StackEntry.Id, SavedStateHandle) -> StackEntry<*>,
        savedStateConfiguration: SavedStateConfiguration,
    ) : ComposeSaver<Stack, SavedState> {
        private val entrySaver = StackEntry.Saver(createRestoredEntry, savedStateConfiguration)

        override fun restore(value: SavedState): Stack {
            return value.read {
                val entries = getSavedStateList(KEY_ENTRIES).map { state -> entrySaver.restore(state) }
                Stack(
                    entries,
                    createEntry,
                )
            }
        }

        override fun SaverScope.save(value: Stack): SavedState {
            return savedState {
                with(entrySaver) {
                    putSavedStateList(KEY_ENTRIES, value.stack.map { entry -> save(entry) })
                }
            }
        }
    }
}
