package com.freeletics.khonshu.navigation.internal

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver as ComposeSaver
import androidx.compose.runtime.saveable.SaverScope
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute

internal class MultiStack @VisibleForTesting internal constructor(
    // Use ArrayList to make sure it is a RandomAccess
    private val allStacks: ArrayList<Stack>,
    private var startStack: Stack,
    private var currentStack: Stack,
    private val createEntry: (BaseRoute) -> StackEntry<*>,
) {
    private val snapshotState: MutableState<StackSnapshot> =
        mutableStateOf(currentStack.snapshot(startStack.rootEntry))
    val snapshot: State<StackSnapshot>
        get() = snapshotState

    val startRoot
        get() = startStack.rootEntry.route

    private fun getBackStack(root: NavRoot): Stack? {
        return allStacks.find { it.id == root.destinationId }
    }

    private fun createBackStack(root: NavRoot): Stack {
        val newStack = Stack.createWith(root, createEntry)
        allStacks.add(newStack)
        return newStack
    }

    private fun removeBackStack(stack: Stack) {
        stack.clear()
        allStacks.remove(stack)
        stack.rootEntry.close()
    }

    internal fun updateVisibleDestinations(notify: Boolean) {
        if (notify) {
            snapshotState.value = currentStack.snapshot(startStack.rootEntry)
        }
    }

    fun push(route: NavRoute, notify: Boolean = true) {
        currentStack.push(route)
        updateVisibleDestinations(notify)
    }

    fun popCurrentStack(notify: Boolean = true) {
        currentStack.pop()
        updateVisibleDestinations(notify)
    }

    fun pop(notify: Boolean = true) {
        if (currentStack.isAtRoot) {
            check(currentStack.id != startStack.id) {
                "Can't navigate back from the root of the start back stack"
            }
            removeBackStack(currentStack)
            currentStack = startStack
            // remove anything that the start stack could have shown before
            // can't use resetToRoot because that will also recreate the root
            currentStack.clear()
        } else {
            currentStack.pop()
        }
        updateVisibleDestinations(notify)
    }

    fun <T : BaseRoute> popUpTo(
        destinationId: DestinationId<T>,
        isInclusive: Boolean,
        notify: Boolean = true,
    ) {
        currentStack.popUpTo(destinationId, isInclusive)
        updateVisibleDestinations(notify)
    }

    fun switchStack(root: NavRoot, clearTargetStack: Boolean, notify: Boolean = true) {
        val stack = getBackStack(root)
        currentStack = if (stack != null) {
            if (clearTargetStack) {
                removeBackStack(stack)
                createBackStack(root)
            } else {
                stack
            }
        } else {
            createBackStack(root)
        }
        if (stack?.id == startStack.id) {
            startStack = currentStack
        }
        updateVisibleDestinations(notify)
    }

    fun replaceAll(root: NavRoot, notify: Boolean = true) {
        // remove all stacks
        while (allStacks.isNotEmpty()) {
            removeBackStack(allStacks.last())
        }

        // create new stack with the root
        val newStack = createBackStack(root)
        startStack = newStack
        currentStack = newStack

        updateVisibleDestinations(notify)
    }

    companion object {
        fun createWith(
            root: NavRoot,
            createEntry: (BaseRoute) -> StackEntry<*>,
        ): MultiStack {
            val startStack = Stack.createWith(root, createEntry)
            return MultiStack(
                allStacks = arrayListOf(startStack),
                startStack = startStack,
                currentStack = startStack,
                createEntry = createEntry,
            )
        }

        private const val KEY_START_STACK = "start_stack"
        private const val KEY_CURRENT_STACK = "current_stack"
        private const val KEY_STACKS = "stacks"
    }

    class Saver(
        private val createEntry: (BaseRoute) -> StackEntry<*>,
        createRestoredEntry: (BaseRoute, StackEntry.Id, SavedStateHandle) -> StackEntry<*>,
    ) : ComposeSaver<MultiStack, SavedState> {
        private val stackSaver = Stack.Saver(createEntry, createRestoredEntry)

        override fun restore(value: SavedState): MultiStack {
            return value.read {
                val savedStacks = getSavedStateList(KEY_STACKS)
                val allStacks = savedStacks.mapTo(ArrayList(savedStacks.size)) {
                    stackSaver.restore(it)
                }
                val startStackId = getString(KEY_START_STACK)
                val startStack = allStacks.first { it.rootEntry.id.value == startStackId }
                val currentStackId = getString(KEY_CURRENT_STACK)
                val currentStack = allStacks.first { it.rootEntry.id.value == currentStackId }

                MultiStack(
                    allStacks = allStacks,
                    startStack = startStack,
                    currentStack = currentStack,
                    createEntry = createEntry,
                )
            }
        }

        override fun SaverScope.save(value: MultiStack): SavedState {
            return savedState {
                putString(KEY_CURRENT_STACK, value.currentStack.rootEntry.id.value)
                putString(KEY_START_STACK, value.startStack.rootEntry.id.value)
                with(stackSaver) {
                    putSavedStateList(KEY_STACKS, value.allStacks.map { save(it) })
                }
            }
        }
    }
}
