package com.freeletics.mad.navigator

import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope

/**
 * [NavigationHandler] can be used to set up navigation with a type of [Navigator]. It is meant to
 * be called once for a given [CoroutineScope] and [NavController] for the initial setup and cancel
 * itself based on the [CoroutineScope].
 *
 * An example usage would be that there is an implementation of [Navigator] that exposes a
 * Kotlin Coroutines `Flow` of navigation events. An implementation of [NavigationHandler]
 * could then `collect` that `Flow` in it's [handle] method and then execute the correct navigation
 * methods on the [NavController] based on the received event.
 *
 * This set up allows to have the general navigation logic (when action x happens navigate to
 * screen y) in your business logic layer, for example as a side effect in your state machine.
 * The business logic would only interact with [Navigator]
 * which does not require a reference
 * to Android framework components. That avoids the risk of leaking an `Activity` or `Fragment` and
 * makes the navigation related logic easily testable on the JVM.
 */
sealed interface NavigationHandler<N : Navigator>

interface FragmentNavigationHandler<N : Navigator> : NavigationHandler<N> {
    /**
     * Called once for the given [scope] and [fragment] to set up navigation with the
     * [navigator]. Any ongoing operation in implementations of this method should be cancelled
     * when the `scope` is cancelled.
     */
    fun handle(
        scope: CoroutineScope,
        fragment: Fragment,
        navigator: N
    )
}

interface ComposeNavigationHandler<N : Navigator> : NavigationHandler<N> {
    /**
     * Called once for the given [scope] and [navController] to set up navigation with the
     * [navigator]. Any ongoing operation in implementations of this method should be cancelled
     * when the `scope` is cancelled.
     */
    fun handle(
        scope: CoroutineScope,
        navController: NavController,
        onBackPressedDispatcher: OnBackPressedDispatcher,
        navigator: N
    )
}
