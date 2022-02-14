package com.freeletics.mad.navigator

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import kotlin.reflect.KClass

/**
 * Represents a navigation event that is being sent by a [NavEventNavigator] and handled by
 * a `NavEventNavigationHandler` implementation. Default implementations of such a handler are
 * provided in separate artifacts.
 *
 * Custom subclasses of `NavEvent` can be sent using [NavEventNavigator.sendNavEvent] but require
 * providing a custom `NavEventNavigationHandler` that supports handling those events.
 */
@VisibleForTesting(otherwise = PACKAGE_PRIVATE)
public sealed interface NavEvent {

    /**
     * Navigates to the given [route].
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public data class NavigateToEvent(
        internal val route: NavRoute,
    ) : NavEvent

    /**
     * Navigates back to the given [popUpTo]. If [inclusive] is `true` the destination
     * itself will also be popped of the back stack. Then navigates to the given [route].
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public data class NavigateToOnTopOfEvent(
        internal val route: NavRoute,
        internal val popUpTo: KClass<out BaseRoute>,
        internal val inclusive: Boolean,
    ) : NavEvent

    /**
     * Navigates to the given [root]. The current back stack will be popped and saved.
     * Whether the backstack of the given route is restored depends on [restoreRootState].
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public data class NavigateToRootEvent(
        internal val root: NavRoot,
        internal val restoreRootState: Boolean,
    ) : NavEvent

    /**
     * Navigates up.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public object UpEvent : NavEvent

    /**
     * Navigates back.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public object BackEvent : NavEvent

    /**
     * Navigates back to the given [popUpTo]. If [inclusive] is `true` the destination itself
     * will also be popped of the back stack.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public data class BackToEvent(
        internal val popUpTo: KClass<out BaseRoute>,
        internal val inclusive: Boolean,
    ) : NavEvent

    /**
     * Launches the [request] to retrieve an event.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public data class ActivityResultEvent<I>(
        internal val request: ActivityResultRequest<I, *>,
        internal val input: I,
    ) : NavEvent

    /**
     * Launches the [request] to retrieve an event.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    public data class PermissionsResultEvent(
        internal val request: PermissionsResultRequest,
        internal val permissions: List<String>,
    ) : NavEvent
}
