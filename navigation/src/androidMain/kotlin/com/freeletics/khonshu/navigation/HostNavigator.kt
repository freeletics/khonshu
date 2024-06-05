package com.freeletics.khonshu.navigation

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freeletics.khonshu.navigation.deeplinks.DeepLink
import com.freeletics.khonshu.navigation.deeplinks.DeepLinkHandler
import com.freeletics.khonshu.navigation.internal.StackEntryStoreViewModel
import com.freeletics.khonshu.navigation.internal.StackSnapshot
import com.freeletics.khonshu.navigation.internal.createHostNavigator
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

/**
 * An implementation of [Navigator] that is meant to be used at the [NavHost] level.
 *
 * An instance can be created by calling [rememberHostNavigator].
 */
public abstract class HostNavigator internal constructor() : Navigator, ResultNavigator, BackInterceptor {
    internal abstract val snapshot: State<StackSnapshot>
    internal abstract val onBackPressedCallback: OnBackPressedCallback

    /**
     * If the given [Intent] was created from a [DeepLink] or the `Uri` returned by [Intent.getData]
     * can be handled using [deepLinkHandlers] and [deepLinkPrefixes] then the navigator will
     * clear the current back stack and navigate to the required destinations.
     */
    public abstract fun handleDeepLink(
        intent: Intent,
        deepLinkHandlers: ImmutableSet<DeepLinkHandler>,
        deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix>,
    )

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
    destinations: ImmutableSet<NavDestination>,
    deepLinkHandlers: ImmutableSet<DeepLinkHandler> = persistentSetOf(),
    deepLinkPrefixes: ImmutableSet<DeepLinkHandler.Prefix> = persistentSetOf(),
): HostNavigator {
    val context = LocalContext.current
    val viewModel = viewModel<StackEntryStoreViewModel>()
    return remember(context, viewModel, startRoot, destinations, deepLinkHandlers, deepLinkPrefixes) {
        createHostNavigator(
            context = context.applicationContext,
            viewModel = viewModel,
            startRoot = startRoot,
            destinations = destinations,
        ).also {
            val handledDeepLinks = viewModel.globalSavedStateHandle.get<Boolean>(SAVED_STATE_HANDLED_DEEP_LINKS)
            if (handledDeepLinks != true) {
                it.handleDeepLink(
                    intent = context.findActivity().intent,
                    deepLinkHandlers = deepLinkHandlers,
                    deepLinkPrefixes = deepLinkPrefixes,
                )
                viewModel.globalSavedStateHandle[SAVED_STATE_HANDLED_DEEP_LINKS] = true
            }
        }
    }
}

private const val SAVED_STATE_HANDLED_DEEP_LINKS = "com.freeletics.khonshu.navigation.handled_deep_links"
