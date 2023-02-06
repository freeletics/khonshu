package com.freeletics.mad.navigator.internal

import android.os.Parcelable
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.ContractResultOwner
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.NavigationResultRequest
import dev.drewhamilton.poko.Poko

/**
 * Represents a navigation event that is being sent by a [NavEventNavigator] and handled by
 * a `NavEventNavigationHandler` implementation. Default implementations of such a handler are
 * provided in separate artifacts.
 *
 * Custom subclasses of `NavEvent` can be sent using [NavEventNavigator.sendNavEvent] but require
 * providing a custom `NavEventNavigationHandler` that supports handling those events.
 */
@InternalNavigatorApi
public sealed interface NavEvent {

    /**
     * Navigates to the given [route].
     */
    @InternalNavigatorApi
    @Poko
    public class NavigateToEvent(
        internal val route: NavRoute,
    ) : NavEvent

    /**
     * Navigates to the given [root]. The current back stack will be popped and saved.
     * Whether the backstack of the given route is restored depends on [restoreRootState].
     */
    @InternalNavigatorApi
    @Poko
    public class NavigateToRootEvent(
        internal val root: NavRoot,
        internal val restoreRootState: Boolean,
        internal val saveCurrentRootState: Boolean,
    ) : NavEvent

    /**
     * Navigates to the given [route].
     */
    @InternalNavigatorApi
    @Poko
    public class NavigateToActivityEvent(
        internal val route: ActivityRoute,
    ) : NavEvent

    /**
     * Navigates up.
     */
    @InternalNavigatorApi
    public object UpEvent : NavEvent

    /**
     * Navigates back.
     */
    @InternalNavigatorApi
    public object BackEvent : NavEvent

    /**
     * Navigates back to the given [popUpTo]. If [inclusive] is `true` the destination itself
     * will also be popped of the back stack.
     */
    @InternalNavigatorApi
    @Poko
    public class BackToEvent(
        internal val popUpTo: DestinationId<*>,
        internal val inclusive: Boolean,
    ) : NavEvent

    /**
     * Launches the [request] to retrieve an event.
     */
    @InternalNavigatorApi
    @Poko
    public class ActivityResultEvent<I>(
        internal val request: ContractResultOwner<I, *, *>,
        internal val input: I,
    ) : NavEvent

    /**
     * Delivers the [result] to the destination that created [key].
     */
    @InternalNavigatorApi
    @Poko
    public class DestinationResultEvent<O : Parcelable>(
        internal val key: NavigationResultRequest.Key<O>,
        internal val result: O,
    ) : NavEvent
}
