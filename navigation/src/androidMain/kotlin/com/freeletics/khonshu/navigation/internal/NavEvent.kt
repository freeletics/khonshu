package com.freeletics.khonshu.navigation.internal

import android.os.Parcelable
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.ContractResultOwner
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.NavigationResultRequest
import dev.drewhamilton.poko.Poko
import kotlin.reflect.KClass

@InternalNavigationTestingApi
public sealed interface NavEvent {

    @InternalNavigationTestingApi
    @Poko
    public class NavigateToEvent(
        internal val route: NavRoute,
    ) : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class NavigateToRootEvent(
        internal val root: NavRoot,
        internal val restoreRootState: Boolean,
    ) : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class NavigateToActivityEvent(
        internal val route: ActivityRoute,
        internal val fallbackRoute: NavRoute?,
    ) : NavEvent

    @InternalNavigationTestingApi
    public data object UpEvent : NavEvent

    @InternalNavigationTestingApi
    public data object BackEvent : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class BackToEvent(
        internal val popUpTo: KClass<out BaseRoute>,
        internal val inclusive: Boolean,
    ) : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class ResetToRoot(
        internal val root: NavRoot,
    ) : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class ReplaceAll(
        internal val root: NavRoot,
    ) : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class ActivityResultEvent<I>(
        internal val request: ContractResultOwner<I, *, *>,
        internal val input: I,
    ) : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class DestinationResultEvent<O : Parcelable>(
        internal val key: NavigationResultRequest.Key<O>,
        internal val result: O,
    ) : NavEvent

    @InternalNavigationTestingApi
    @Poko
    public class MultiNavEvent(
        internal val navEvents: List<NavEvent>,
    ) : NavEvent
}
