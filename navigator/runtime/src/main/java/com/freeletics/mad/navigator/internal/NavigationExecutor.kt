package com.freeletics.mad.navigator.internal

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import com.freeletics.mad.navigator.NavigationResultRequest

@InternalNavigatorApi
public interface NavigationExecutor {
    public fun navigate(route: NavRoute)
    public fun navigate(root: NavRoot, restoreRootState: Boolean)
    public fun navigate(route: ActivityRoute)
    public fun navigateUp()
    public fun navigateBack()
    public fun navigateBackTo(destinationId: DestinationId<*>, isInclusive: Boolean)
    public fun deliverResult(key: NavigationResultRequest.Key<*>, result: Parcelable)
    public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
    public fun savedStateHandleFor(destinationId: DestinationId<*>): SavedStateHandle
    public fun viewModelStoreFor(destinationId: DestinationId<*>): ViewModelStore
}
