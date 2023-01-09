package com.freeletics.mad.navigator

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PACKAGE_PRIVATE
import androidx.annotation.VisibleForTesting.PRIVATE
import com.freeletics.mad.navigator.internal.DestinationId
import dev.drewhamilton.poko.Poko
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
    @Poko
    public class NavigateToEvent(
        internal val route: NavRoute,
    ) : NavEvent

    /**
     * Navigates to the given [root]. The current back stack will be popped and saved.
     * Whether the backstack of the given route is restored depends on [restoreRootState].
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    @Poko
    public class NavigateToRootEvent(
        internal val root: NavRoot,
        internal val restoreRootState: Boolean,
        internal val saveCurrentRootState: Boolean,
    ) : NavEvent

    /**
     * Navigates to the given [route].
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    @Poko
    public class NavigateToActivityEvent(
        internal val route: ActivityRoute,
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
    @Poko
    public class BackToEvent(
        internal val popUpTo: DestinationId<*>,
        internal val inclusive: Boolean,
    ) : NavEvent

    // TODO: remove after introducing a testing artifact
    @VisibleForTesting(otherwise = PRIVATE)
    public companion object {
        public fun BackToEvent(
            popUpTo: KClass<out NavRoute>,
            inclusive: Boolean,
        ): BackToEvent = BackToEvent(DestinationId(popUpTo), inclusive)
    }

    /**
     * Launches the [request] to retrieve an event.
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    @Poko
    public class ActivityResultEvent<I>(
        internal val request: ContractResultOwner<I, *, *>,
        internal val input: I,
    ) : NavEvent

    /**
     * Delivers the [result] to the destination that created [key].
     */
    @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
    @Poko
    public class DestinationResultEvent<O : Parcelable>(
        internal val key: NavigationResultRequest.Key<O>,
        internal val result: O,
    ) : NavEvent
}
