package com.freeletics.mad.navigator.internal

import android.os.Parcelable
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.ContractResultOwner
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.NavigationResultRequest
import dev.drewhamilton.poko.Poko

@InternalNavigatorApi
public sealed interface NavEvent {

    @InternalNavigatorApi
    @Poko
    public class NavigateToEvent(
        internal val route: NavRoute,
    ) : NavEvent

    @InternalNavigatorApi
    @Poko
    public class NavigateToRootEvent(
        internal val root: NavRoot,
        internal val restoreRootState: Boolean,
    ) : NavEvent

    @InternalNavigatorApi
    @Poko
    public class NavigateToActivityEvent(
        internal val route: ActivityRoute,
    ) : NavEvent

    @InternalNavigatorApi
    public object UpEvent : NavEvent

    @InternalNavigatorApi
    public object BackEvent : NavEvent

    @InternalNavigatorApi
    @Poko
    public class BackToEvent(
        internal val popUpTo: DestinationId<*>,
        internal val inclusive: Boolean,
    ) : NavEvent

    @InternalNavigatorApi
    @Poko
    public class ResetToRoot(
        internal val root: NavRoot,
    ) : NavEvent

    @InternalNavigatorApi
    @Poko
    public class ActivityResultEvent<I>(
        internal val request: ContractResultOwner<I, *, *>,
        internal val input: I,
    ) : NavEvent

    @InternalNavigatorApi
    @Poko
    public class DestinationResultEvent<O : Parcelable>(
        internal val key: NavigationResultRequest.Key<O>,
        internal val result: O,
    ) : NavEvent
}
