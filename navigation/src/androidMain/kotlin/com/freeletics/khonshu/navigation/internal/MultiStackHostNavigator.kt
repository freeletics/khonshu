package com.freeletics.khonshu.navigation.internal

import android.content.Intent
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResult
import com.freeletics.khonshu.navigation.NavigationResultRequest
import com.freeletics.khonshu.navigation.Navigator
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.extractDeepLinkRoutes
import kotlin.reflect.KClass
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class MultiStackHostNavigator(
    private val stack: MultiStack,
    viewModel: StackEntryStoreViewModel,
) : HostNavigator() {
    override val snapshot: State<StackSnapshot>
        get() = stack.snapshot

    init {
        viewModel.globalSavedStateHandle.setSavedStateProvider(SAVED_STATE_STACK) {
            stack.saveState()
        }
    }

    override fun handleDeepLink(
        intent: Intent,
        deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
        deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
    ): Boolean {
        val deepLinkRoutes = intent.extractDeepLinkRoutes(deepLinkHandlers, deepLinkPrefixes)
        return handleDeepLink(deepLinkRoutes)
    }

    @VisibleForTesting
    internal fun handleDeepLink(deepLinkRoutes: List<BaseRoute>): Boolean {
        if (deepLinkRoutes.isEmpty()) {
            return false
        }

        stack.switchStack(stack.startRoot, clearTargetStack = true)

        deepLinkRoutes.forEachIndexed { index, route ->
            when (route) {
                is NavRoot -> {
                    require(index == 0) { "NavRoot can only be the first element of a deep link" }
                    require(route.destinationId != stack.startRoot.destinationId) {
                        "$route is the start root which is not allowed to be part of a deep " +
                            "link because it will always be on the back stack"
                    }
                    stack.switchStack(route, clearTargetStack = true)
                }
                is NavRoute -> stack.push(route)
            }
        }

        return true
    }

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

    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        val entry = snapshot.value.entryFor(key.destinationId)
        entry.savedStateHandle[key.requestKey] = NavigationResult(result)
    }

    override fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        val requestKey = "${id.route.qualifiedName!!}-$resultType"
        val key = NavigationResultRequest.Key<O>(id, requestKey)
        return NavigationResultRequest(key, snapshot.value.entryFor(id).savedStateHandle)
    }

    override val onBackPressedCallback = DelegatingOnBackPressedCallback()

    override fun <T> backPresses(value: T): Flow<T> {
        return callbackFlow {
            val onBackPressed = {
                check(trySendBlocking(value).isSuccess)
            }

            onBackPressedCallback.addCallback(onBackPressed)

            awaitClose {
                onBackPressedCallback.removeCallback(onBackPressed)
            }
        }
    }

    override fun navigate(block: Navigator.() -> Unit) {
        val nonNotifyingNavigator = NonNotifyingNavigator()
        nonNotifyingNavigator.apply(block)
        stack.updateVisibleDestinations(true)
    }

    internal companion object {
        const val SAVED_STATE_STACK = "com.freeletics.khonshu.navigation.stack"
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
    }
}
