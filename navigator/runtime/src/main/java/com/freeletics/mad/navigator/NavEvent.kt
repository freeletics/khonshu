package com.freeletics.mad.navigator

import androidx.annotation.IdRes
import androidx.navigation.NavOptions

/**
 * Represents a navigation event that is being sent by a [NavEventNavigator] and handled by
 * a `NavEventNavigationHandler` implementation. Default implementations of such a handler are
 * provided in separate artifacts.
 *
 * Custom subclasses of `NavEvent` can be sent using [NavEventNavigator.sendNavEvent] but require
 * providing a custom `NavEventNavigationHandler` that supports handling those events.
 */
public interface NavEvent {

    /**
     * Navigates to the given [navRoute] using the optionally passed [navOptions].
     */
    public data class NavigateToEvent(
        val navRoute: NavRoute,
        val navOptions: NavOptions? = null,
    ) : NavEvent

    /**
     * Navigates up.
     */
    public object UpEvent : NavEvent

    /**
     * Navigates back.
     */
    public object BackEvent : NavEvent

    /**
     * Navigates back to the given [destinationId]. If [inclusive] is `true` the destination itself
     * will also be popped of the back stack.
     */
    public data class BackToEvent(
        @IdRes val destinationId: Int,
        val inclusive: Boolean,
    ) : NavEvent

    /**
     * Launches the [resultLauncher] to retrieve an event.
     */
    public data class ResultLauncherEvent<I>(
        val resultLauncher: ResultLauncher<I>,
        val input: I,
    ) : NavEvent
}
