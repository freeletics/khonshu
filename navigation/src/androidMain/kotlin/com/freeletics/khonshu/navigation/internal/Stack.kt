package com.freeletics.khonshu.navigation.internal

import android.os.Bundle
import androidx.core.os.bundleOf
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute

internal class Stack private constructor(
    initialStack: List<StackEntry<*>>,
    private val createEntry: (BaseRoute) -> StackEntry<*>,
    private val onStackEntryRemoved: (StackEntry.Id) -> Unit,
) {
    private val stack = ArrayDeque<StackEntry<*>>(20).also {
        it.addAll(initialStack)
    }

    val id: DestinationId<*> get() = rootEntry.destinationId
    val rootEntry: StackEntry<*> get() = stack.first()
    val isAtRoot: Boolean get() = !stack.last().removable

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
        return stack.findLast { it.destinationId == destinationId } as StackEntry<T>?
    }

    fun snapshot(startStackId: DestinationId<*>): StackSnapshot {
        return StackSnapshot(stack.toList(), startStackId == id)
    }

    fun push(route: NavRoute) {
        stack.add(createEntry(route))
    }

    fun pop() {
        check(stack.last().removable) { "Can't pop the root of the back stack" }
        popInternal()
    }

    private fun popInternal() {
        val entry = stack.removeLast()
        onStackEntryRemoved(entry.id)
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

    fun saveState(): Bundle {
        val ids = ArrayList<String>(stack.size)
        val routes = ArrayList<BaseRoute>(stack.size)
        stack.forEach {
            ids.add(it.id.value)
            routes.add(it.route)
        }
        return bundleOf(
            SAVED_STATE_IDS to ids,
            SAVED_STATE_ROUTES to routes,
        )
    }

    companion object {
        fun createWith(
            root: NavRoot,
            createEntry: (BaseRoute) -> StackEntry<*>,
            onStackEntryRemoved: (StackEntry.Id) -> Unit,
        ): Stack {
            val rootEntry = createEntry(root)
            return Stack(listOf(rootEntry), createEntry, onStackEntryRemoved)
        }

        fun fromState(
            bundle: Bundle,
            createEntry: (BaseRoute) -> StackEntry<*>,
            createRestoredEntry: (BaseRoute, StackEntry.Id) -> StackEntry<*>,
            onStackEntryRemoved: (StackEntry.Id) -> Unit,
        ): Stack {
            val ids = bundle.getStringArrayList(SAVED_STATE_IDS)!!

            @Suppress("DEPRECATION")
            val routes = bundle.getParcelableArrayList<BaseRoute>(SAVED_STATE_ROUTES)!!
            val entries = ids.mapIndexed { index, id ->
                createRestoredEntry(routes[index], StackEntry.Id(id))
            }
            return Stack(entries, createEntry, onStackEntryRemoved)
        }

        private const val SAVED_STATE_IDS = "com.freeletics.khonshu.navigation.stack.ids"
        private const val SAVED_STATE_ROUTES = "com.freeletics.khonshu.navigation.stack.routes"
    }
}
