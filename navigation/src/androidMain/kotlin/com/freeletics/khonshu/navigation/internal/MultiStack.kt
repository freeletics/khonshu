package com.freeletics.khonshu.navigation.internal

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute

internal class MultiStack(
    // Use ArrayList to make sure it is a RandomAccess
    private val allStacks: ArrayList<Stack>,
    private var startStack: Stack,
    private var currentStack: Stack,
    private val createEntry: (BaseRoute) -> StackEntry<*>,
    private val inputRoot: NavRoot,
) {
    private val snapshotState: MutableState<StackSnapshot> =
        mutableStateOf(currentStack.snapshot(startStack.rootEntry))
    val snapshot: State<StackSnapshot>
        get() = snapshotState

    val startRoot
        get() = startStack.rootEntry.route as NavRoot

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

    fun saveState(): Bundle {
        return bundleOf(
            SAVED_STATE_ALL_STACKS to ArrayList(allStacks.map { it.saveState() }),
            SAVED_STATE_CURRENT_STACK to currentStack.id.route.java,
            SAVED_STATE_START_STACK to startStack.id.route.java,
            SAVED_INPUT_ROOT to inputRoot,
        )
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
                inputRoot = root,
            )
        }

        @Suppress("DEPRECATION")
        fun fromState(
            root: NavRoot?,
            bundle: Bundle,
            createEntry: (BaseRoute) -> StackEntry<*>,
            createRestoredEntry: (BaseRoute, StackEntry.Id, SavedStateHandle) -> StackEntry<*>,
        ): MultiStack {
            val inputRoot = bundle.getParcelable<NavRoot>(SAVED_INPUT_ROOT)!!

            if (root != null && inputRoot != root) {
                return createWith(
                    root = root,
                    createEntry = createEntry,
                )
            }

            val allStackBundles = bundle.getParcelableArrayList<Bundle>(SAVED_STATE_ALL_STACKS)!!
            val currentStackId = bundle.getSerializable(SAVED_STATE_CURRENT_STACK)!!
            val startDestinationId = bundle.getSerializable(SAVED_STATE_START_STACK)!!

            val allStacks = allStackBundles.mapTo(ArrayList(allStackBundles.size)) {
                Stack.fromState(it, createEntry, createRestoredEntry)
            }
            val startStack = allStacks.first { it.id.route.java == startDestinationId }
            val currentStack = allStacks.first { it.id.route.java == currentStackId }

            return MultiStack(
                allStacks = allStacks,
                startStack = startStack,
                currentStack = currentStack,
                createEntry = createEntry,
                inputRoot = inputRoot,
            )
        }

        private const val SAVED_STATE_ALL_STACKS = "com.freeletics.khonshu.navigation.stack.all_stacks"
        private const val SAVED_STATE_CURRENT_STACK = "com.freeletics.khonshu.navigation.stack.current_stack"
        private const val SAVED_STATE_START_STACK = "com.freeletics.khonshu.navigation.stack.start_stack"
        private const val SAVED_INPUT_ROOT = "com.freeletics.khonshu.navigation.stack.input_root"
    }
}
