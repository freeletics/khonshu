package com.freeletics.mad.navigator.internal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
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
    public fun navigateBackTo(destinationId: DestinationId<*>, isInclusive: Boolean)
    public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
    public fun savedStateHandleFor(destinationId: DestinationId<*>): SavedStateHandle
    public fun storeFor(destinationId: DestinationId<*>): Store

    @InternalNavigatorApi
    public interface Store {
        public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T
    }
}
