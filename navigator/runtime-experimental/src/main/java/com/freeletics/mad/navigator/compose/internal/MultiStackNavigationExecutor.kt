package com.freeletics.mad.navigator.compose.internal

import android.os.Parcelable
import androidx.compose.runtime.State
import androidx.lifecycle.SavedStateHandle
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.internal.DestinationId
import com.freeletics.mad.navigator.internal.NavigationExecutor
import com.freeletics.mad.navigator.internal.destinationId

internal class MultiStackNavigationExecutor(
    private val stack: MultiStack,
    private val viewModel: StoreViewModel,
    private val activityStarter: (ActivityRoute) -> Unit,
    deepLinkRoutes: List<Parcelable>,
) : NavigationExecutor {

    @Suppress("unused") //TODO
    val visibleEntries: State<List<StackEntry<*>>>
        get() = stack.visibleEntries

    @Suppress("unused") //TODO
    val canNavigateBack: State<Boolean>
        get() = stack.canNavigateBack

    init {
        if (deepLinkRoutes.isNotEmpty()) {
            stack.resetToRoot(stack.startRoot)
            deepLinkRoutes.forEachIndexed { index, route ->
                when (route) {
                    is NavRoot -> {
                        require(index == 0) { "NavRoot can only be the first element of a deep link" }
                        require(route.destinationId != stack.startRoot.destinationId) {
                            "$route is the start root which is not allowed to be part of a deep " +
                                    "link because it will always be on the back stack"
                        }
                        stack.push(route, clearTargetStack = true)
                    }
                    is NavRoute -> stack.push(route)
                    is ActivityRoute -> navigate(route)
                }
            }
        }

        viewModel.globalSavedStateHandle.setSavedStateProvider(SAVED_STATE_STACK) {
            val stackState = stack.saveState()
            // if this line is reached the stackState contains the deep links already
            stackState.putBoolean(SAVED_STATE_HANDLED_DEEP_LINKS, true)
            stackState
        }
    }

    override fun navigate(route: NavRoute) {
        stack.push(route)
    }

    override fun navigate(root: NavRoot, restoreRootState: Boolean) {
        stack.push(root, clearTargetStack = !restoreRootState)
    }

    override fun navigate(route: ActivityRoute) {
        activityStarter(route)
    }

    override fun navigateUp() {
        stack.popCurrentStack()
    }

    override fun navigateBack() {
        stack.pop()
    }

    override fun <T : BaseRoute> navigateBackTo(
        destinationId: DestinationId<T>,
        isInclusive: Boolean
    ) {
        stack.popUpTo(destinationId, isInclusive)
    }

    override fun resetToRoot(root: NavRoot) {
        stack.resetToRoot(root)
    }

    override fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T {
        return entryFor(destinationId).route
    }

    override fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle {
        val entry = entryFor(destinationId)
        return viewModel.provideSavedStateHandle(entry.id)
    }

    override fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): NavigationExecutor.Store {
        val entry = entryFor(destinationId)
        return storeFor(entry.id)
    }

    @Suppress("unused", "unused_parameter") //TODO
    fun storeFor(entryId: StackEntry.Id): NavigationExecutor.Store {
        return viewModel.provideStore(entryId)
    }

    private fun <T : BaseRoute> entryFor(destinationId: DestinationId<T>): StackEntry<T> {
        return stack.entryFor(destinationId) ?:
            throw IllegalStateException("Route $destinationId not found on back stack")
    }

    internal companion object {
        const val SAVED_STATE_STACK = "com.freeletics.mad.navigator.stack"
        const val SAVED_STATE_HANDLED_DEEP_LINKS = "com.freeletics.mad.navigator.handled_deep_links"
    }
}
