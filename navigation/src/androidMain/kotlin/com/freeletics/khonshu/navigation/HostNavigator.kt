package com.freeletics.khonshu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.StackEntryStoreViewModel
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import com.freeletics.khonshu.navigation.internal.createHostNavigator
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

/**
 * An implementation of [Navigator] that is meant to be used at the [NavHost] level.
 *
 * An instance can be created by calling [createHostNavigator].
 */
public abstract class HostNavigator internal constructor() : Navigator {
    internal abstract val snapshot: State<StackSnapshot>
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
    destinations: ImmutableSet<NavDestination>,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler> = persistentSetOf(),
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix> = persistentSetOf(),
): HostNavigator {
    val context = LocalContext.current
    val viewModel = viewModel<StackEntryStoreViewModel>()
    return remember(context, viewModel, startRoot, destinations, deepLinkHandlers, deepLinkPrefixes) {
        createHostNavigator(
            context = context.applicationContext,
            intent = context.findActivity().intent,
            viewModel = viewModel,
            startRoot = startRoot,
            destinations = destinations,
            deepLinkHandlers = deepLinkHandlers,
            deepLinkPrefixes = deepLinkPrefixes,
        )
    }
}
