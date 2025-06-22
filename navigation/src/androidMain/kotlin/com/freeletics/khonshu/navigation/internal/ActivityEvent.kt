package com.freeletics.khonshu.navigation.internal

import com.freeletics.khonshu.navigation.NavRoute
import com.freeletics.khonshu.navigation.activity.ActivityResultContractRequest
import com.freeletics.khonshu.navigation.activity.ActivityRoute
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
        internal val request: ActivityResultContractRequest<I, *, *>,
        internal val input: I,
    ) : ActivityEvent
}
