package com.freeletics.mad.navigation.internal

import android.os.Parcelable
import com.freeletics.mad.navigation.ActivityRoute
import com.freeletics.mad.navigation.ContractResultOwner
import com.freeletics.mad.navigation.NavRoot
import com.freeletics.mad.navigation.NavRoute
import com.freeletics.mad.navigation.NavigationResultRequest
import dev.drewhamilton.poko.Poko

@InternalNavigationApi
public sealed interface NavEvent {

    @InternalNavigationApi
    @Poko
    public class NavigateToEvent(
        internal val route: NavRoute,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class NavigateToRootEvent(
        internal val root: NavRoot,
        internal val restoreRootState: Boolean,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class NavigateToActivityEvent(
        internal val route: ActivityRoute,
    ) : NavEvent

    @InternalNavigationApi
    public object UpEvent : NavEvent

    @InternalNavigationApi
    public object BackEvent : NavEvent

    @InternalNavigationApi
    @Poko
    public class BackToEvent(
        internal val popUpTo: DestinationId<*>,
        internal val inclusive: Boolean,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class ResetToRoot(
        internal val root: NavRoot,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class ActivityResultEvent<I>(
        internal val request: ContractResultOwner<I, *, *>,
        internal val input: I,
    ) : NavEvent

    @InternalNavigationApi
    @Poko
    public class DestinationResultEvent<O : Parcelable>(
        internal val key: NavigationResultRequest.Key<O>,
        internal val result: O,
    ) : NavEvent
}
