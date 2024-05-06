package com.freeletics.khonshu.navigation.internal

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.State
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.HostNavigator
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResultRequest
import com.freeletics.khonshu.navigation.StandaloneNavigationResultRequest
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.extractDeepLinkRoutes
import kotlin.reflect.KClass
import kotlinx.collections.immutable.ImmutableSet

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

    override fun <O : android.os.Parcelable> deliverNavigationResult(key: NavigationResultRequest.Key<O>, result: O) {
        snapshot.value.entryFor(key.destinationId).savedStateHandle[key.requestKey] = result
    }

    override fun <T : BaseRoute, O : android.os.Parcelable> registerForNavigationResultInternal(
        id: DestinationId<T>,
        resultType: String,
    ): NavigationResultRequest<O> {
        val requestKey = "${id.route.qualifiedName!!}-$resultType"
        val key = NavigationResultRequest.Key<O>(id, requestKey)
        return StandaloneNavigationResultRequest(key, snapshot.value.entryFor(key.destinationId).savedStateHandle)
    }

    internal companion object {
        const val SAVED_STATE_STACK = "com.freeletics.khonshu.navigation.stack"
    }
}
