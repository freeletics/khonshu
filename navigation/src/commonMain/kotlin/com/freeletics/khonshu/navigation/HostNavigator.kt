package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.deeplinks.handleDeepLink
import com.freeletics.khonshu.navigation.deeplinks.obtainLaunchInfo
import com.freeletics.khonshu.navigation.internal.InternalNavigationCodegenApi
import com.freeletics.khonshu.navigation.internal.InternalNavigationTestingApi
import com.freeletics.khonshu.navigation.internal.MultiStackHostNavigator
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import com.freeletics.khonshu.navigation.internal.createMultiStack
import com.freeletics.khonshu.navigation.internal.rememberMultiStack

public abstract class HostNavigator @InternalNavigationTestingApi constructor() : Navigator {
    @InternalNavigationTestingApi
    public abstract val snapshot: State<StackSnapshot>

    @InternalNavigationTestingApi
    public abstract val startRoot: NavRoot

    /**
     * Allows to group multiple navigation actions and execute them atomically. The state of this [HostNavigator] will
     * only be updated after running all actions. This should be used when navigating multiple times, for example
     * calling `navigateBackTo` followed by `navigateTo`.
     */
    public abstract fun navigate(block: Navigator.() -> Unit)
}

/**
 * Returns an instance of [HostNavigator] with the given [destinations] and
 * [startRoot] as initially shown destination.
 *
 * To support deep links a set of [DeepLinkHandlers][DeepLinkHandler] can be passed in optionally.
 * These will be used to build the correct back stack when the current `Activity` was launched with
 * an `ACTION_VIEW` `Intent` that contains an url in it's data. [deepLinkPrefixes] can be used to
 * provide a default set of url patterns that should be matched by any [DeepLinkHandler] that
 * doesn't provide its own [DeepLinkHandler.prefixes].
 */
@Composable
public fun rememberHostNavigator(
    startRoot: NavRoot,
    destinations: Set<NavDestination<*>>,
    deepLinkHandlers: Set<DeepLinkHandler> = setOf(),
    deepLinkPrefixes: Set<DeepLinkHandler.Prefix> = setOf(),
): HostNavigator {
    val multiStack by rememberMultiStack(startRoot, destinations)
    val handledDeepLinks = rememberSaveable { mutableStateOf(false) }
    return remember(multiStack, deepLinkHandlers, deepLinkPrefixes) {
        MultiStackHostNavigator(multiStack)
    }.also {
        if (!handledDeepLinks.value) {
            it.handleDeepLink(
                launchInfo = obtainLaunchInfo(destinations),
                deepLinkHandlers = deepLinkHandlers,
                deepLinkPrefixes = deepLinkPrefixes,
            )
            handledDeepLinks.value = true
        }
    }
}

internal val LocalHostNavigator: ProvidableCompositionLocal<HostNavigator> =
    staticCompositionLocalOf {
        throw IllegalStateException("Can't access HostNavigator outside of a NavHost")
    }

@InternalNavigationCodegenApi
public fun createHostNavigator(
    startRoot: NavRoot,
    destinations: Set<NavDestination<*>>,
    savedStateHandle: SavedStateHandle,
): HostNavigator {
    val stack = createMultiStack(startRoot, destinations, savedStateHandle)
    return MultiStackHostNavigator(stack = stack)
}
