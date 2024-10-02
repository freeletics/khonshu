package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.ContractResultOwner
import com.freeletics.khonshu.navigation.NavRoute
import dev.drewhamilton.poko.Poko

@InternalNavigationTestingApi
public sealed interface ActivityEvent {
    @InternalNavigationTestingApi
    @Poko
    public class NavigateTo(
        internal val route: ActivityRoute,
        internal val fallbackRoute: NavRoute?,
    ) : ActivityEvent

    @InternalNavigationTestingApi
    @Poko
    public class NavigateForResult<I>(
        internal val request: ContractResultOwner<I, *, *>,
        internal val input: I,
    ) : ActivityEvent
}
