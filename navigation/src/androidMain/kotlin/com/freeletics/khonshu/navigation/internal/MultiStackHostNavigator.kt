package com.freeletics.khonshu.navigation.internal

import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.Navigator
import kotlin.reflect.KClass

internal actual class MultiStackHostNavigator actual constructor(
    private val stack: MultiStack,
) : HostNavigator() {
    override val snapshot: State<StackSnapshot>
        get() = stack.snapshot
    override val startRoot: NavRoot
        get() = snapshot.value.startRoot.route

    override fun navigateTo(route: NavRoute) {
        stack.push(route)
    }

    override fun navigateUp() {
        stack.popCurrentStack()
    }

    override fun navigateBack() {
        stack.pop()
    }

    override fun <T : BaseRoute> navigateBackTo(
        popUpTo: KClass<T>,
        inclusive: Boolean,
    ) {
        stack.popUpTo(DestinationId(popUpTo), inclusive)
    }

    override fun switchBackStack(root: NavRoot) {
        stack.switchStack(root, clearTargetStack = false)
    }

    override fun showRoot(root: NavRoot) {
        stack.switchStack(root, clearTargetStack = true)
    }

    override fun replaceAllBackStacks(root: NavRoot) {
        stack.replaceAll(root)
    }

    override fun navigate(block: Navigator.() -> Unit) {
        val nonNotifyingNavigator = NonNotifyingNavigator()
        nonNotifyingNavigator.apply(block)
        stack.updateVisibleDestinations(true)
    }

    @InternalNavigationApi
    override fun getTopEntryFor(destinationId: DestinationId<*>): StackEntry<*> {
        return snapshot.value.entryFor(destinationId)
    }

    @InternalNavigationApi
    override fun getEntryFor(id: StackEntry.Id): StackEntry<*> {
        return snapshot.value.entryFor(id)
    }

    private inner class NonNotifyingNavigator : Navigator {
        override fun navigateTo(route: NavRoute) {
            stack.push(route, notify = false)
        }

        override fun navigateUp() {
            stack.popCurrentStack(notify = false)
        }

        override fun navigateBack() {
            stack.pop(notify = false)
        }

        override fun <T : BaseRoute> navigateBackTo(popUpTo: KClass<T>, inclusive: Boolean) {
            stack.popUpTo(DestinationId(popUpTo), inclusive, notify = false)
        }

        override fun switchBackStack(root: NavRoot) {
            stack.switchStack(root, clearTargetStack = false, notify = false)
        }

        override fun showRoot(root: NavRoot) {
            stack.switchStack(root, clearTargetStack = true, notify = false)
        }

        override fun replaceAllBackStacks(root: NavRoot) {
            stack.replaceAll(root, notify = false)
        }

        @InternalNavigationApi
        override fun getTopEntryFor(destinationId: DestinationId<*>): StackEntry<*> {
            return this@MultiStackHostNavigator.getTopEntryFor(destinationId)
        }

        @InternalNavigationApi
        override fun getEntryFor(id: StackEntry.Id): StackEntry<*> {
            return this@MultiStackHostNavigator.getEntryFor(id)
        }
    }
}
