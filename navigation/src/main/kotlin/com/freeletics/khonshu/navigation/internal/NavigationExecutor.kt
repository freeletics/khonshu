package com.freeletics.khonshu.navigation.internal

import androidx.lifecycle.SavedStateHandle
import com.freeletics.khonshu.navigation.ActivityRoute
import com.freeletics.khonshu.navigation.BaseRoute
import com.freeletics.khonshu.navigation.NavRoot
import com.freeletics.khonshu.navigation.NavRoute
import kotlin.reflect.KClass

@InternalNavigationApi
public interface NavigationExecutor {
    public fun navigate(route: NavRoute)
    public fun navigate(root: NavRoot, restoreRootState: Boolean)
    public fun navigate(route: ActivityRoute)
    public fun navigateUp()
    public fun navigateBack()
    public fun <T : BaseRoute> navigateBackTo(destinationId: DestinationId<T>, isInclusive: Boolean)
    public fun resetToRoot(root: NavRoot)
    public fun <T : BaseRoute> routeFor(destinationId: DestinationId<T>): T
    public fun <T : BaseRoute> savedStateHandleFor(destinationId: DestinationId<T>): SavedStateHandle
    public fun <T : BaseRoute> storeFor(destinationId: DestinationId<T>): Store

    @InternalNavigationApi
    public interface Store {
        public fun <T : Any> getOrCreate(key: KClass<T>, factory: () -> T): T
    }
}
