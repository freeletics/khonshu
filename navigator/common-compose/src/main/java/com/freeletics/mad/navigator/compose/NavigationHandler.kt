package com.freeletics.mad.navigator.compose

import androidx.compose.runtime.Composable
import com.freeletics.mad.navigator.Navigator

/**
 * [NavigationHandler] can be used to set up navigation with a type of [Navigator]. It is meant to
 * be added to the root `Composable` of a screen.
 *
 * An example usage would be that there is an implementation of [Navigator] that exposes a
 * Kotlin Coroutines `Flow` of navigation events. An implementation of `NavigationHandler`
 * could then `collect` that `Flow` in it's `handle` method and then execute the correct navigation
 * methods on a `NavController` based on the received event.
 *
 * This set up allows to have the general navigation logic (when action x happens navigate to
 * screen y) in your business logic layer, for example as a side effect in your state machine.
 * The business logic would only interact with [Navigator] which does not require a reference
 * to Android framework components. That avoids the risk of leaking an `Activity` or `Fragment` and
 * makes the navigation related logic easily testable on the JVM.
 */
public interface NavigationHandler<N : Navigator> {
    /**
     * Part of the root of the composition of a screen. This should be used to set up navigation with
     * the [navigator]. Any ongoing operation in implementations of this method should be cancelled
     * when it leaves the composition.
     */
    @Composable
    public fun Navigation(navigator: N)
}
