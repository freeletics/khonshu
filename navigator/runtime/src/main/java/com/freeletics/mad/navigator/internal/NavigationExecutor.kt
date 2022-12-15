package com.freeletics.mad.navigator.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute

@InternalNavigatorApi
public interface NavigationExecutor {
    public fun navigate(route: NavRoute)
    public fun navigate(root: NavRoot, restoreRootState: Boolean, saveCurrentRootState: Boolean)
    public fun navigate(route: ActivityRoute)
    public fun navigateUp()
    public fun navigateBack()
    public fun <T : BaseRoute> navigateBackTo(destinationId: DestinationId<T>, isInclusive: Boolean)
    public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
    public fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle
    public fun <T : BaseRoute> viewModelStoreFor(destinationId: DestinationId<T>): ViewModelStore
}
