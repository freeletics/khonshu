package com.freeletics.khonshu.navigation.internal

import android.content.Intent
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResultRequest
import com.freeletics.khonshu.navigation.Navigator
import com.freeletics.khonshu.navigation.StandaloneNavigationResultRequest
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
    private val activityStarter: (ActivityRoute) -> Unit,
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
    ) {
        val deepLinkRoutes = intent.extractDeepLinkRoutes(deepLinkHandlers, deepLinkPrefixes)
        handleDeepLink(deepLinkRoutes)
    }

    @VisibleForTesting
    internal fun handleDeepLink(deepLinkRoutes: List<Parcelable>) {
        if (deepLinkRoutes.isEmpty()) {
            return
        }

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
                is ActivityRoute -> navigateTo(route)
            }
        }
    }

    override fun navigateTo(route: NavRoute) {
        stack.push(route)
    }

    override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
        stack.push(root, clearTargetStack = !restoreRootState)
    }

    override fun navigateTo(route: ActivityRoute) {
        activityStarter(route)
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

    override fun resetToRoot(root: NavRoot) {
        stack.resetToRoot(root)
    }

    override fun replaceAll(root: NavRoot) {
        stack.replaceAll(root)
    }

    override fun <O : Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        snapshot.value.entryFor(key.destinationId).savedStateHandle[key.requestKey] = result
    }

    override fun <T : BaseRoute, O : Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        val requestKey = "${id.route.qualifiedName!!}-$resultType"
        val key = NavigationResultRequest.Key<O>(id, requestKey)
        return StandaloneNavigationResultRequest(key, snapshot.value.entryFor(key.destinationId).savedStateHandle)
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
        nonNotifyingNavigator.activityRoutes.forEach { activityStarter(it) }
    }

    internal companion object {
        const val SAVED_STATE_STACK = "com.freeletics.khonshu.navigation.stack"
    }

    private inner class NonNotifyingNavigator : Navigator {
        val activityRoutes = mutableListOf<ActivityRoute>()

        override fun navigateTo(route: NavRoute) {
            stack.push(route, notify = false)
        }

        override fun navigateTo(route: ActivityRoute) {
            activityRoutes += route
        }

        override fun navigateToRoot(root: NavRoot, restoreRootState: Boolean) {
            stack.push(root, clearTargetStack = !restoreRootState, notify = false)
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

        override fun resetToRoot(root: NavRoot) {
            stack.resetToRoot(root, notify = false)
        }

        override fun replaceAll(root: NavRoot) {
            stack.replaceAll(root, notify = false)
        }
    }
}
