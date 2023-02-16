package com.freeletics.mad.navigator.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.mad.navigator.ActivityRoute
import com.freeletics.mad.navigator.BaseRoute
import com.freeletics.mad.navigator.NavRoot
import com.freeletics.mad.navigator.NavRoute
import kotlin.reflect.KClass

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
    public fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): Store

    @InternalNavigatorApi
    public interface Store {
        public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T
    }
}
