package com.freeletics.mad.navigator.compose.internal

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.bundleOf
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.compose.ContentDestination
import com.freeletics.mad.navigator.internal.DestinationId
import com.freeletics.mad.navigator.internal.destinationId
import java.util.UUID

internal class MultiStack(
    private val allStacks: MutableList<Stack>,
    private val startStack: Stack,
    private var currentStack: Stack,
    private val destinations: List<ContentDestination<*>>,
    private val onStackEntryRemoved: (StackEntry.Id) -> Unit,
    private val idGenerator: () -> String,
) {

    private val visibleEntryState: MutableState<List<StackEntry<*>>> =
        mutableStateOf(currentStack.computeVisibleEntries())
    val visibleEntries: State<List<StackEntry<*>>>
        get() = visibleEntryState

    private val canNavigateBackState: MutableState<Boolean> =
        mutableStateOf(canNavigateBack())
    val canNavigateBack: State<Boolean>
        get() = canNavigateBackState

    val startRoot = startStack.rootEntry.route as NavRoot

    fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T>? {
        val entry = currentStack.entryFor(destinationId)
        if (entry != null) {
            return entry
        }

        // the root of the default back stack is always on the back stack
        if (startStack.rootEntry.destinationId == destinationId) {
            @Suppress("UNCHECKED_CAST")
            return startStack.rootEntry as StackEntry<T>
        }

        return null
    }

    private fun getOrCreateBackStack(root: NavRoot): Stack {
        val existingStack = allStacks.find { it.id == root.destinationId }
        if (existingStack != null) {
            return existingStack
        }

        val newStack = Stack.createWith(root, destinations, onStackEntryRemoved, idGenerator)
        allStacks.add(newStack)
        return newStack
    }

    private fun switchToBackStack(
        stack: Stack,
        restoreRootState: Boolean,
        saveCurrentRootState: Boolean
    ) {
        if (!saveCurrentRootState) {
            currentStack.clear()
            // if the current stack is not the start stack, completely remove it so that the root
            // gets cleared and that navigating to this stack will create a new instance of root
            if (currentStack != startStack) {
                allStacks.remove(currentStack)
                onStackEntryRemoved(currentStack.rootEntry.id)
            }
        }
        if (!restoreRootState) {
            stack.clear()
        }
        currentStack = stack
    }

    private fun updateVisibleDestinations() {
        visibleEntryState.value = currentStack.computeVisibleEntries()
        canNavigateBackState.value = canNavigateBack()
    }

    private fun canNavigateBack(): Boolean {
        return currentStack.id != startStack.id || !currentStack.isAtRoot
    }

    fun push(route: NavRoute) {
        currentStack.push(route)
        updateVisibleDestinations()
    }

    fun push(root: NavRoot, restoreRootState: Boolean, saveCurrentRootState: Boolean) {
        val stack = getOrCreateBackStack(root)
        switchToBackStack(stack, restoreRootState, saveCurrentRootState)
        updateVisibleDestinations()
    }

    fun popCurrentStack() {
        currentStack.pop()
        updateVisibleDestinations()
    }

    fun pop() {
        if (currentStack.isAtRoot) {
            check(currentStack.id != startStack.id) {
                "Can't navigate back from the root of the start back stack"
            }
            switchToBackStack(startStack, restoreRootState = false, saveCurrentRootState = false)
        } else {
            currentStack.pop()
        }
        updateVisibleDestinations()
    }

    fun <T : BaseRoute> popBackTo(
        destinationId: DestinationId<T>,
        isInclusive: Boolean
    ) {
        currentStack.popUpTo(destinationId, isInclusive)
        updateVisibleDestinations()
    }

    fun saveState(): Bundle {
        return bundleOf(
            SAVED_STATE_ALL_STACKS to ArrayList(allStacks.map { it.saveState() }),
            SAVED_STATE_CURRENT_STACK to currentStack.id.route.java,
        )
    }

    companion object {
        fun createWith(
            root: NavRoot,
            destinations: List<ContentDestination<*>>,
            onStackEntryRemoved: (StackEntry.Id) -> Unit,
            idGenerator: () -> String = { UUID.randomUUID().toString() },
        ): MultiStack {
            val startStack = Stack.createWith(root, destinations, onStackEntryRemoved, idGenerator)
            return MultiStack(
                allStacks = mutableListOf(startStack),
                startStack = startStack,
                currentStack = startStack,
                destinations = destinations,
                onStackEntryRemoved = onStackEntryRemoved,
                idGenerator = idGenerator,
            )
        }

        @Suppress("DEPRECATION")
        fun fromState(
            root: NavRoot,
            bundle: Bundle,
            destinations: List<ContentDestination<*>>,
            onStackEntryRemoved: (StackEntry.Id) -> Unit,
            idGenerator: () -> String = { UUID.randomUUID().toString() },
        ): MultiStack {
            val allStackBundles = bundle.getParcelableArrayList<Bundle>(SAVED_STATE_ALL_STACKS)!!
            val currentStackId = bundle.getSerializable(SAVED_STATE_CURRENT_STACK)
            val allStacks = allStackBundles.mapTo(ArrayList(allStackBundles.size)) {
                Stack.fromState(it, destinations, onStackEntryRemoved, idGenerator)
            }
            val startStack = allStacks.first { it.id == root.destinationId }
            val currentStack = allStacks.first { it.id.route.java == currentStackId }
            return MultiStack(
                allStacks = allStacks,
                startStack = startStack,
                currentStack = currentStack,
                destinations = destinations,
                onStackEntryRemoved = onStackEntryRemoved,
                idGenerator = idGenerator,
            )
        }

        private const val SAVED_STATE_ALL_STACKS = "com.freeletics.mad.navigator.stack.all_stacks"
        private const val SAVED_STATE_CURRENT_STACK = "com.freeletics.mad.navigator.stack.current_stack"
    }
}
